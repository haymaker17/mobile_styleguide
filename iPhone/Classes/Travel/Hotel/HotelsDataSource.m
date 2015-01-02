
//
//  HotelsNearMeDataSource.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelsDataSource.h"
#import "SearchCriteriaTableViewCell.h"
#import "SearchCriteriaEditableTableViewCell.h"
#import "SearchTableHeaderCellData.h"
#import "CTEHotelCellData.h"
#import "CTEHotelSearch.h"
#import "CTEHotel.h"
#import "CTEDateUtility.h"
#import "CTENetworkSettings.h"
#import "HotelSearchCriteriaV2.h"
#import "LoadingSpinnerCellData.h"
#import "AnalyticsTracker.h"




@interface HotelsDataSource()

@property (nonatomic, strong) NSMutableArray *searchCriteriaCells;
@property (nonatomic, strong) NSMutableArray *searchResultList;
@property (nonatomic, strong) NSMutableArray *searchResultListCopy;     // Copy of the result for filter

@property (nonatomic, strong) SearchTableHeaderCellData *searchHeaderCellData ;
@property (nonatomic, strong) HotelSearchCriteriaV2 *hotelSearchCriteria;

// Internal Array to maintain sections
// Each section consists of another Arry of objects.
@property (nonatomic, strong) NSMutableArray *sourceSections;
@property (nonatomic, strong) NSMutableArray *sectionTitles;
@property (nonatomic, strong) NSDateFormatter *dateFormatter;

@property (nonatomic, strong) NSString *searchResultSectionTitle;

//
// For Google Analytics
@property (nonatomic, strong) NSMutableDictionary *topFiveRecommendations;

@property (nonatomic) int stayDurationInDays;

@end

@implementation HotelsDataSource

NSString* const CHECKIN_DATE_STRING = @"Check-In Date";
NSString* const CHECKOUT_DATE_STRING = @"Check-Out Date";

-(instancetype)init
{
    self = [super init];
    
    if (!self)
        return nil;
    
    self.searchCriteriaCells = [[NSMutableArray alloc] init];
    self.searchResultList = [[NSMutableArray alloc] init];
    self.sourceSections = [[NSMutableArray alloc] init];
    self.sectionTitles = [[NSMutableArray alloc] init];
    self.stayDurationInDays = 1;
    
    // By default set the dates to tonight
    _hotelSearchCriteria = [[HotelSearchCriteriaV2 alloc] init];
    
    CLLocation *currentLocation = [GlobalLocationManager sharedInstance].currentLocation;
    // Set up search criteria
    // Hotels for tonight. initialize hotelSearchCritera accordingly
    NSDate *checkInDate = [CTEDateUtility addDaysToDate:[NSDate date] daysToAdd:0];
    NSDate *checkOutDate = [CTEDateUtility addDaysToDate:checkInDate daysToAdd:1];
    
    
    // First time get the current location by default.
    //
    if (currentLocation != nil) {
        _hotelSearchCriteria.latitude = currentLocation.coordinate.longitude;
        _hotelSearchCriteria.longitude = currentLocation.coordinate.latitude;

    }
    
    _hotelSearchCriteria.checkinDate = checkInDate;
    _hotelSearchCriteria.checkoutDate = checkOutDate;
    // default 
    _hotelSearchCriteria.distanceValue = 5;
    _hotelSearchCriteria.hotelName = @"";

    [self loadSearchHeaderCells];
    
    return self;

 }

-(void)loadContent
{
    // Load hotels near me first
    [self searchHotelsNearMe];
}

-(void)searchHotelsNearMe
{

    [self searchHotels:[[@"Hotels near you" localize] uppercaseString]];
}
// Method SearchHotels that reads into Self.SearchCriteria cells and create a seach query.
// This method is called from viewcontroller
-(void)searchHotels
{
    [self searchHotels:@""];
}

/*!
 Makes Hotel Search request to MWS
 
 */

- (void)searchHotels:(NSString *)sectionHeaderText
{
    DLog(@"CTENetworkSettings.serverURL: %@", [[CTENetworkSettings sharedInstance] serverURL]);
    
    _searchResultSectionTitle = sectionHeaderText;
    // When the Search request is fired show a loading spinner cell in the search section
    if ( [self isSearchCriteriaValid] ) {
        // set to loading state
        self.dataSourceState = kDataLoading;
        [self updateViewState];
        
        CTEHotelSearch *request = [[CTEHotelSearch alloc] initWithHotelName:self.hotelSearchCriteria.hotelName
                                                                   latitude:self.hotelSearchCriteria.latitude
                                                                  longitude:self.hotelSearchCriteria.longitude
                                                                checkInDate:self.hotelSearchCriteria.checkinDate
                                                               checkOutDate:self.hotelSearchCriteria.checkoutDate];
        
         DLog(@"Calling HotelSearch for Hotel Criteria... ");
        // get search results without polling
        // This function is now a blocking call until the full resultset is available
//        [request searchWithSuccess:^(NSArray *cteHotelList){
//            self.dataSourceState = kDataLoadComplete;
//            [self showHotelSearchResults:cteHotelList];
//            if (self.hideWaitView) {
//                self.hideWaitView();
//            };
//        }failure:^(CTEError *error){
//            ALog(@" Unable to get any hotel search results");
//            self.dataSourceState = kDataLoadError;
//            // TODO: Show error message in the search result section
//            if (self.hideWaitView){
//                self.hideWaitView();
//            }
//            if (self.onSearchError) {
//                self.onSearchError(@"Looks like we are experience some difficulties. Please check back in a few minutes");
//            }
//            [self updateViewState];
//        }];
        
        self.searchDone = NO;

        
//        [request searchWithSuccess:^(NSArray *cteHotelList){
        
        // Make async call with polling
        [request asyncSearchWithSuccess:^(NSArray *cteHotelList, BOOL searchDone){
            //self.searchDone = YES;
            self.searchDone = searchDone;
            
            // Also when searchDone == YES, use only the latest hotelList to display and throw the previous list away
            self.dataSourceState = kDataLoadComplete;
            
            if ([cteHotelList count] > 0) {
                //
                // polling call then Clear the self.SearchResultList before adding new results
                //
                [self.searchResultList removeAllObjects];
                
                for (CTEHotel *hotelEntry in cteHotelList) {
                    DLog(@"Hotel Property Name: %@", hotelEntry.propertyName);
                    CTEHotelCellData *cteHotelCellData = [[CTEHotelCellData alloc] initWithCTEHotel:hotelEntry];
                    [self.searchResultList addObject:cteHotelCellData];
                }
            }
            else
            {
                // Handle no results state
                ALog(@"No Hotels were found show an alert message");
                if (self.onSearchError) {
                    self.onSearchError([@"We couldnt find any hotels with the given search criteria. Please try again with different criteria." localize]);
                }
            }
            // Notify the UI to change the view accordingly
            [self updateViewState];

            
            if (self.hideWaitView && searchDone ) {
                self.hideWaitView();
            };
            
        }failure:^(CTEError *error){    // Handle failure
            
            ALog(@" Unable to get any hotel search results");
            self.dataSourceState = kDataLoadError;
            // TODO: Show error message in the search result section
            if (self.hideWaitView){
                self.hideWaitView();
            }
            if (self.onSearchError) {
                // Default message to show user
                NSString *userMessage = [@"Looks like we are experiencing some difficulties. Please check back in a few minutes." localize];
                // Check if the error has a userMessage inside it
                if (error.concurErrorMessages != nil && [error.concurErrorMessages count] > 0)
                {
                    // Grab the first error message
                    CTEErrorMessage *firstMessage = error.concurErrorMessages[0];
                    
                    // Make sure the user message has some weight to it
                    if (firstMessage != nil && firstMessage.userMessage != nil && [firstMessage.userMessage length] > 5)
                    {
                        // Do not localize this as Snowbird has already performed localization based on company settings
                        userMessage = firstMessage.userMessage;
                    }
                }
                self.onSearchError(userMessage);
            }
            [self updateViewState];
        }];
    }
}



-(void)showHotelSearchResults:(NSArray *)hotels
{
#warning this method crashes if delegate is invalid
    
  }

/*! This method updates the loading spinner cell.
 */
// BUG : This method is called twice. everytime the loading spinner cell is removed and added.
-(void)updateViewState
{
    
    switch (self.dataSourceState) {
            
        case kDataLoading:
            
            // Clean section header always for search results
            if ([_sectionTitles count] > 1) {
                _sectionTitles[1] = @"";
            }
            else
                [_sectionTitles addObject:@""];

            // Add a section with loading spinner cell
            [self.delegate dataSourceWillChangeData:self];
            
            // Clean any existing/old search results if any
            if ([self.sourceSections containsObject:self.searchResultList]) {       // Check before cleaning

                [self.searchResultList removeAllObjects];
                [self.sourceSections removeObject:self.searchResultList];
                [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:1 forChangeType:kDataSourceChangeDelete];

            }

            [self.delegate dataSourceDidChangeContent:self];
            
            break;
        
        case kDataLoadPaused:
            // No action for now
            break;
            
        case kDataLoadComplete:
            // If the view as already showing the search results then update view to show new results.
            // step 1 : Remove the old search results section and insert new section.
            [self.delegate dataSourceWillChangeData:self] ;
            if (![self isSearchCriteriaSectionVisbile] ) {
                if ([self.sourceSections containsObject:self.searchResultList]) {
                    // Notify UI to stop animation
                    NSUInteger indexOfResultsList = [self.sourceSections indexOfObject:self.searchResultList];
                    [self.sourceSections removeObject:self.searchResultList];
                    [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:indexOfResultsList forChangeType:kDataSourceChangeDelete];
                    
                 }
                
                //MOB-20616 : Do not sort until polling is complete. 
                if (self.searchDone) {
                    // update the section header if there are any results
                    if ([_sectionTitles count] > 1 ) {
                        self.sectionTitles[1] = _searchResultSectionTitle ;
                    }
                    else
                        [_sectionTitles addObject:_searchResultSectionTitle];

                    [self sortSearchResultListByDefault];  // This is the default sort order
                    [self defaultFilter];
                    if(self.afterDoneSearch){
                        self.afterDoneSearch();
                    }
                }
                
                self.hotelListSection = [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.searchResultList];
                // Then add the new section back
                [self.sourceSections addObject:self.searchResultList];
                [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:[self.sourceSections count]-1 forChangeType:kDataSourceChangeInsert];
            }
            
           [self.delegate dataSourceDidChangeContent:self];
            
           break;
            
        case kDataLoadResume:
 
            break;
            
            // This is the case whenever Criteria is expanded.
        case kDataInitSearch:
            
            break;
        case kDataLoadError:
            [self.delegate dataSourceWillChangeData:self] ;
            // if there was an error remove any stale results showing as a result of polling
            if (![self isSearchCriteriaSectionVisbile] ) {
                if ([self.sourceSections containsObject:self.searchResultList]) {
                    // Notify UI to stop animation
                    NSUInteger indexOfResultsList = [self.sourceSections indexOfObject:self.searchResultList];
                    [self.sourceSections removeObject:self.searchResultList];
                    [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:indexOfResultsList forChangeType:kDataSourceChangeDelete];
                    
                }
            }
            [self.delegate dataSourceDidChangeContent:self];

            break;
            
        default:
            break;
    }

}

-(BOOL)isSearchCriteriaValid
{
    return [self.hotelSearchCriteria isHotelSearchCriteriaValid];
}

- (BOOL) isSpecificCity
{
    // This is an ugly implementation, need to polish this later -- Ray
    return ![self.searchHeaderCellData.location isEqualToString:[@"Current Location" localize]];
}

#pragma mark - properties
/*! Always returns the HeaderCelldata formatted based on latst search criteria
*/
-(SearchTableHeaderCellData *)searchHeaderCellData
{
    
    //TODO: Improvise this later
    NSString *checkinDateString = [CTEDateUtility convertDateToString:self.hotelSearchCriteria.checkinDate withOutputFormat:@"EEE MMM dd" timeZone:[NSTimeZone localTimeZone]];
    NSString *checkoutDateString = [CTEDateUtility convertDateToString:self.hotelSearchCriteria.checkoutDate withOutputFormat:@"EEE MMM dd" timeZone:[NSTimeZone localTimeZone]];
    _searchHeaderCellData.stayDatesString = [NSString stringWithFormat:@"%@ - %@", checkinDateString, checkoutDateString ];
    _searchHeaderCellData.cellIdentifier = @"SearchCriteriaTableHeaderCell";
    _searchHeaderCellData.imageName = @"icon_nav_location";
    _searchHeaderCellData.cellHeight = 70;

    return _searchHeaderCellData;
}

/**
 Return the latest hotelSearchCriteria
 */
-(HotelSearchCriteriaV2 *)hotelSearchCriteria
{
    return _hotelSearchCriteria;
}

/**
 * Always return the listSection built from hotel search results
 */
-(ConcreteDataSourceSectionInfo *)hotelListSection
{
    return  [[ConcreteDataSourceSectionInfo alloc] initWithArray:_searchResultList];
}

-(BOOL)isSearchCriteriaSectionVisbile
{
    return ![self.searchCriteriaCells containsObject:self.searchHeaderCellData];
}

-(BOOL)isHotelSearchListSectionVisible
{
    return [self.sourceSections containsObject:self.searchResultList];
}


#pragma mark - load and toggle methods

// Load the content of this data source.
- (void)loadSearchHeaderCells
{
	// Initialize the searchHeaderCell here.
     _searchHeaderCellData = [[SearchTableHeaderCellData alloc] init];
    
    //default start with no location. if location service is available/ or if user chooses a location then it this will be updated with current location.
      _searchHeaderCellData.location = [@"Choose a location" localize];
    // SearchCriteria cells will have either header or criteria cells but not both.
    // Start with header first
     [self.searchCriteriaCells addObject:self.searchHeaderCellData];
    
    
    // Search Criteria is always added as first section
    [self.sourceSections insertObject:self.searchCriteriaCells atIndex:0];
    //
    // empty means no section title for this section
    [self.sectionTitles insertObject:@"" atIndex:0];
}

/*!
 Inserts a search criteria cells for user input
 */
-(void)insertSearchCriteriaCells:(NSIndexPath *)startIndexPath
{
    // TODO : Make the cells editable
    // store the search criteria data in object hotelSearchCriteria for later implementation
    
    SearchCriteriaCellData *cellData = [[SearchCriteriaCellData alloc] init];
    NSInteger row;
    NSInteger section;
    
    cellData.title = [@"Destination" localize];
    cellData.cellName = @"Destination";
    //TODO:  This should be set by the current location
    cellData.subTitle = _searchHeaderCellData.location;
    cellData.imageName = @"icon_destination";
    cellData.cellIdentifier = @"SearchCriteriaCell";
    cellData.cellHeight = 60;
    cellData.cellType = kLocationList;
    
    
    if (startIndexPath != nil) {
        row = startIndexPath.row;
        section = startIndexPath.section;
        [self insertItemAtIndexPath:cellData indexPath:[NSIndexPath indexPathForRow:row++ inSection:section]];
        
    }
    else
    {
        [self.searchCriteriaCells addObject:cellData];
    }

    
    cellData = [[SearchCriteriaCellData alloc] init];
    
    
    cellData.title = [@"Check-In Date" localize];
    cellData.subTitle = [CTEDateUtility convertDateToString:self.hotelSearchCriteria.checkinDate withOutputFormat:@"MM/dd/yyyy" timeZone:[NSTimeZone localTimeZone]];
    cellData.imageName = @"icon_calendar";
    cellData.cellIdentifier = @"SearchCriteriaCell";
    cellData.cellType = kDatePicker;
    cellData.cellHeight = 60;
    NSMutableDictionary *keyValPairs = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.hotelSearchCriteria.checkinDate,@"cellvalue", nil];
    keyValPairs[@"minDate"] = [NSDate date];
    keyValPairs[@"maxDate"] = [CTEDateUtility addDaysToDate:[NSDate date] daysToAdd:355];
    cellData.keyValue = keyValPairs;
    cellData.cellName = CHECKIN_DATE_STRING;
    
    if (startIndexPath != nil) {
        [self insertItemAtIndexPath:cellData indexPath:[NSIndexPath indexPathForRow:row++ inSection:section]];
        
    }
    else
    {
        [self.searchCriteriaCells addObject:cellData];
    }

    
    cellData = [[SearchCriteriaCellData alloc] init];
    
    cellData.title = [@"Check-Out Date" localize];
    cellData.subTitle = [CTEDateUtility convertDateToString:self.hotelSearchCriteria.checkoutDate withOutputFormat:@"MM/dd/yyyy" timeZone:[NSTimeZone localTimeZone]];
    cellData.imageName = @"icon_calendar";
    cellData.cellIdentifier = @"SearchCriteriaCell";
    cellData.cellType = kDatePicker;
    cellData.cellHeight = 60;
    keyValPairs = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.hotelSearchCriteria.checkoutDate,@"cellvalue", nil];
    keyValPairs[@"minDate"] = [CTEDateUtility addDaysToDate:[NSDate date] daysToAdd:1];
    keyValPairs[@"maxDate"] = [CTEDateUtility addDaysToDate:[NSDate date] daysToAdd:356];
    cellData.keyValue = keyValPairs;
    cellData.cellName = CHECKOUT_DATE_STRING;

    if (startIndexPath != nil) {
        [self insertItemAtIndexPath:cellData indexPath:[NSIndexPath indexPathForRow:row++ inSection:section]];
    }
    else
    {
        [self.searchCriteriaCells addObject:cellData];
    }

    
    cellData = [[SearchCriteriaCellData alloc] init];
    
    cellData.title = [@"Hotels Within" localize];
    cellData.subTitle = @"10 Miles";
    cellData.imageName = @"icon_locator";
    cellData.cellIdentifier = @"SearchCriteriaCell";
    cellData.cellType = kPickerView;
    cellData.cellHeight = 60;
    cellData.cellName = @"Hotels Within";
    
    if (startIndexPath != nil) {
         [self insertItemAtIndexPath:cellData indexPath:[NSIndexPath indexPathForRow:row++ inSection:section]];
    }
    else
    {
        [self.searchCriteriaCells addObject:cellData];
    }
    
    //MOB-19875 Remove the Hotel Name Containing Cell
}


-(void)toggleSearchCriteriaSection
{
    // Toggle's Search Cells based on which cell is in search list array
    //
    [self.delegate dataSourceWillChangeData:self] ;
    
    
    if (![self isSearchCriteriaSectionVisbile]) {  // Expand
        // remove Search header and then add other cells
        // By default header cell is in Row 0 and section 0 --> to Start with
        
        NSIndexPath *indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
        
        [self removeItemAtIndexPath:indexPath];
        [self insertSearchCriteriaCells:indexPath];
        // Hide the Hotels list section when showing Search Criteria
        if ([self isHotelSearchListSectionVisible]) {
            NSUInteger index = [self.sourceSections indexOfObject:self.searchResultList];
            [self.sourceSections removeObject:self.searchResultList];
             [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:index forChangeType:kDataSourceChangeDelete];
        }
        if (self.dataSourceState == kDataLoading || self.dataSourceState == kDataLoadResume  ) {
            // Loading means there was a network request already fired and spinner is showing. so set the state to paused and refresh
            self.dataSourceState = kDataLoadPaused;
        }
       
    }
    else //Collapse - Already showing search criterial cells
    {
        NSArray *searchCells = [self.searchCriteriaCells copy];
        //
        // Start deleting rows from end to avoid NSIndexPath issues
        int count = (int)[searchCells count];
        for (int itemIndex = count - 1 ; itemIndex >= 0 ; itemIndex-- ) {
            NSArray *indexPaths = [self indexPathsForItem:searchCells[itemIndex]];
            if (indexPaths != nil) {
                for (NSIndexPath *indexPath in indexPaths) {
                    if (indexPaths != nil) {
                        [self removeItemAtIndexPath:indexPath];
                    }
                }   // End indexpaths
                
            }
        }    // End deletion for loop
        // Now insert the hearder row
        [self insertItemAtIndexPath:self.searchHeaderCellData indexPath:[NSIndexPath indexPathForRow:0 inSection:0]];
        // show the hotel list section
        if (![self isHotelSearchListSectionVisible] && [self.hotelListSection numberOfObjects] > 0) {
            [self.sourceSections addObject:self.searchResultList];
            [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:[self.sourceSections indexOfObject:self.searchResultList ] forChangeType:kDataSourceChangeInsert];
        }
        if (self.dataSourceState == kDataLoadPaused) {
            // if we paused then set it to loading. has potential of race condition and end up with invalid state if hotel results come at the same time
            self.dataSourceState = kDataLoadResume;
        }
    }
    [self.delegate dataSourceDidChangeContent:self];
    [self updateViewState];
}

#pragma mark - update methods


/*! Update Search Criteria class when search button is pressed.
 *  Must be called before doing the search otherwise this searchHotels will pick up stale query
 */
-(void)updateHotelSearchCriteria
{
    // when search button is pressed look into each searchSearchCriteriaData.KeyValue and update the hotelSearchCriteria
    // TODO : Lots of error check
    for (int index = 0; index < [self.searchCriteriaCells count]; index++) {
        
        SearchCriteriaCellData *cellData = self.searchCriteriaCells[index];
        if (index == 0) {
            // if index = 0 then its a location
            CTELocation *location = (CTELocation *)cellData.keyValue[@"cellvalue"];
            if (location != nil) {
                _searchHeaderCellData.location = location.location;
                _hotelSearchCriteria.latitude = location.latitude;
                _hotelSearchCriteria.longitude = location.longitude;

            }
        }
        if ([cellData.cellName isEqualToString:CHECKIN_DATE_STRING]) {
            NSDate *checkinDate = (NSDate *)cellData.keyValue[@"cellvalue"];
            _hotelSearchCriteria.checkinDate = checkinDate;
        }
        if ([cellData.cellName isEqualToString:CHECKOUT_DATE_STRING]) {//index == 2) {
            // checkout date
            NSDate *checkoutDate = (NSDate *)cellData.keyValue[@"cellvalue"];
            _hotelSearchCriteria.checkoutDate = checkoutDate;

        }
    }
}

//updates hotelnames clause in searchCriteria
-(void)updateHotelNameCriteria:(NSString *)hotelName
{
    _hotelSearchCriteria.hotelName = hotelName;
}

/**
 * Get the hotel name for searching
 */
- (NSString *)getHotelNameCriteria;
{
    return _hotelSearchCriteria.hotelName;
}

// Update destination in search Criteria
-(void)updateDestination:(CTELocation *)cteLocation
{
    SearchCriteriaCellData *destinationData = nil;
    // Get destination cell from searchCriteria cells
    for (SearchCriteriaCellData *cellData in self.searchCriteriaCells) {
        if (cellData.cellType == kLocationList) {
            destinationData = cellData;
            break;
        }
    }
    // Stupid way to save keyvalue pairs.
    NSIndexPath *indexPath  = [self indexPathsForItem:destinationData][0];
    // Need null-check as it is possible
    if (indexPath != nil)
    {
        destinationData.keyValue = [[NSDictionary alloc] initWithObjectsAndKeys:cteLocation,@"cellvalue", nil];
        destinationData.subTitle = cteLocation.location;
        [self.delegate dataSourceWillChangeData:self];
        [self.delegate dataSource:self didChangeObject:destinationData atIndexPath:indexPath forChangeType:kDataSourceChangeUpdate newIndexPath:nil];
        [self.delegate dataSourceDidChangeContent:self];
    }
    
}


#pragma mark -  datepicker methods

-(void)insertDatePicker:(NSIndexPath *)index
{
    // insert a date datePickerCell
    SearchCriteriaCellData *datePickerCellData = [[SearchCriteriaCellData alloc] init];
    datePickerCellData.cellIdentifier = @"datePickerCell";
    datePickerCellData.cellHeight = 150;
    // Differentiate between checkin and checkout dates.
    NSIndexPath *previousCellIndexPath = [NSIndexPath indexPathForRow:index.row inSection:index.section];
    SearchCriteriaCellData *previousCellData = [self itemAtIndexPath:previousCellIndexPath];
    
    // set cell name
    if ([previousCellData.cellName isEqualToString:CHECKOUT_DATE_STRING]) {
        datePickerCellData.cellName = @"Check-Out Date Picker";
    }
    else if ([previousCellData.cellName isEqualToString:CHECKIN_DATE_STRING]){
        datePickerCellData.cellName = @"Check-In Date Picker";
    }
    
    if ([previousCellData.keyValue objectForKey:@"cellvalue"] != nil) {
        datePickerCellData.keyValue = [previousCellData.keyValue copy];
    }
    else{
        NSMutableDictionary *keyValDictionary = [[NSMutableDictionary alloc] initWithDictionary:previousCellData.keyValue];
        if (index.row == 1) {
            keyValDictionary[@"cellvalue"] = _hotelSearchCriteria.checkinDate;
        } else
        {
            keyValDictionary[@"cellvalue"] = _hotelSearchCriteria.checkoutDate;
        }
        datePickerCellData.keyValue = keyValDictionary;
    }

    NSLog(@"index.row = %ld , value = %@", (long)index.row,self.searchCriteriaCells[index.row] );
    [self.searchCriteriaCells insertObject:datePickerCellData atIndex:index.row + 1];
    
}


-(void)insertSearchDistancePicker:(NSIndexPath *)index
{
    // insert a date datePickerCell
    SearchCriteriaCellData *searchDistancePickerCellData = [[SearchCriteriaCellData alloc] init];
    searchDistancePickerCellData.cellIdentifier = @"distancePickerCell";
    searchDistancePickerCellData.cellHeight = 140;
    NSLog(@"index.row = %ld , value = %@", (long)index.row,self.searchCriteriaCells[index.row] );
    [self.searchCriteriaCells insertObject:searchDistancePickerCellData atIndex:index.row + 1];
}

-(void)removeDatePicker:(NSIndexPath *)index
{
    // remove a date datePickerCell
    [self.searchCriteriaCells removeObjectAtIndex:index.row + 1];
    
}
- (void) updateDate:(NSDate *)targetDate atIndexPath:(NSIndexPath *)indexPath
{
    SearchCriteriaCellData *cellData = [self itemAtIndexPath:indexPath];
    
    // update cell data for selected date picker first
    cellData.subTitle =  [CTEDateUtility convertDateToString:targetDate withOutputFormat:@"MM/dd/yyyy" timeZone:[NSTimeZone localTimeZone]];
    NSMutableDictionary *keyValuePairs = [[NSMutableDictionary alloc] initWithDictionary:cellData.keyValue];
    keyValuePairs[@"cellvalue"] = targetDate;
    cellData.keyValue = keyValuePairs;
    
    // when check-in date is selected
    if ([cellData.cellName isEqualToString:CHECKIN_DATE_STRING]) {
        DLog(@"Check-in Date");
        NSIndexPath *checkOutCellIndexPath = [NSIndexPath indexPathForRow:indexPath.row + 2 inSection:indexPath.section];
        SearchCriteriaCellData *checkOutCellData = [self itemAtIndexPath:checkOutCellIndexPath];

        NSDate *newDate = [CTEDateUtility addDaysToDate:targetDate daysToAdd:self.stayDurationInDays];
        NSDate *maxCheckoutDate = [checkOutCellData.keyValue objectForKey:@"maxDate"];
        if ([maxCheckoutDate compare:newDate] != NSOrderedDescending) { // if check-in Date + stay duration > max checkout date
            self.stayDurationInDays = (int)[self daysBetweenDate:targetDate andDate:maxCheckoutDate];
        }
        [self changeDateCellDataWithDate:targetDate dayInterval:self.stayDurationInDays cellData:checkOutCellData];
        [self.delegate dataSourceWillChangeData:self];
        [self.delegate dataSource:self didChangeObject:cellData atIndexPath:indexPath forChangeType:kDataSourceChangeUpdate newIndexPath:nil];
        [self.delegate dataSource:self didChangeObject:cellData atIndexPath:checkOutCellIndexPath forChangeType:kDataSourceChangeUpdate newIndexPath:nil];
        [self.delegate dataSourceDidChangeContent:self];
        return;
    }
    else{ // check-out date cell is selected
        DLog(@"Check-Out Date");
        NSIndexPath *checkInCellIndexPath = [NSIndexPath indexPathForRow:indexPath.row - 1 inSection:indexPath.section];
        SearchCriteriaCellData *checkInCellData = [self itemAtIndexPath:checkInCellIndexPath];
        NSDate *checkInDate = [checkInCellData.keyValue objectForKey:@"cellvalue"];
        
        if ([self daysBetweenDate:targetDate andDate:checkInDate] >= 0) {
            [self changeDateCellDataWithDate:targetDate dayInterval:-1 cellData:checkInCellData];
            [self.delegate dataSourceWillChangeData:self];
            [self.delegate dataSource:self didChangeObject:cellData atIndexPath:indexPath forChangeType:kDataSourceChangeUpdate newIndexPath:nil];
            [self.delegate dataSource:self didChangeObject:cellData atIndexPath:checkInCellIndexPath forChangeType:kDataSourceChangeUpdate newIndexPath:nil];
            [self.delegate dataSourceDidChangeContent:self];
            self.stayDurationInDays = 1;
            return;
        }
        else {
            self.stayDurationInDays = (int) [self daysBetweenDate:checkInDate andDate:targetDate];
        }
            
    }
    
    [self.delegate dataSourceWillChangeData:self];
    [self.delegate dataSource:self didChangeObject:cellData atIndexPath:indexPath forChangeType:kDataSourceChangeUpdate newIndexPath:nil];
    [self.delegate dataSourceDidChangeContent:self];
}

- (NSInteger)daysBetweenDate:(NSDate*)fromDateTime andDate:(NSDate*)toDateTime
{
    NSDate *fromDate;
    NSDate *toDate;
    
    NSCalendar *calendar = [NSCalendar currentCalendar];
    
    [calendar rangeOfUnit:NSDayCalendarUnit startDate:&fromDate
                 interval:NULL forDate:fromDateTime];
    [calendar rangeOfUnit:NSDayCalendarUnit startDate:&toDate
                 interval:NULL forDate:toDateTime];
    
    NSDateComponents *difference = [calendar components:NSDayCalendarUnit
                                               fromDate:fromDate toDate:toDate options:0];
    
    return [difference day];
}

-(void)changeDateCellDataWithDate:(NSDate *)date dayInterval:(int)numOfDay cellData:(SearchCriteriaCellData *)cellData
{
    NSDate *newDate = [CTEDateUtility addDaysToDate:date daysToAdd:numOfDay];
    cellData.subTitle =  [CTEDateUtility convertDateToString:newDate withOutputFormat:@"MM/dd/yyyy" timeZone:[NSTimeZone localTimeZone]];
    NSMutableDictionary *replacementKeyVal = [[NSMutableDictionary alloc] initWithDictionary:cellData.keyValue];
    replacementKeyVal[@"cellvalue"] = newDate;
    cellData.keyValue = replacementKeyVal;
}



#pragma mark - base class methods.
- (NSArray *)indexPathsForItem:(id)object
{
    NSInteger row = 0;
    NSInteger section = 0;
    NSIndexPath *indexPath = nil;
    
    // Really brute force.
    // Write better logic later
    for (NSArray *cellArray in self.sourceSections ) {
        
        for (id cellData in cellArray) {
            if ([cellData isEqual:object]) {
                indexPath = [NSIndexPath indexPathForRow:row inSection:section];
                break;
            }
            row++;
        }
        section++;
    }
    
    // Check if nil, otherwise app crashes
    if (indexPath != nil){
        return @[indexPath];
    }
    else{
        return nil;
    }
}


- (void)removeItemAtIndexPath:(NSIndexPath *)indexPath
{
    id object = [self itemAtIndexPath:indexPath];
    
    [self.sourceSections[indexPath.section] removeObjectAtIndex:indexPath.row];
    [self.delegate dataSource:self didChangeObject:object atIndexPath:indexPath forChangeType:kDataSourceChangeDelete newIndexPath:nil];
    
    return;
}



-(void)insertItemAtIndexPath:(id)item indexPath:(NSIndexPath *)indexPath
{
    
    [self.sourceSections[indexPath.section] insertObject:item atIndex:indexPath.row];
    [self.delegate dataSource:self didChangeObject:item atIndexPath:nil forChangeType:kDataSourceChangeInsert newIndexPath:indexPath];
    
}

-(id)itemAtIndexPath:(NSIndexPath *)indexPath
{
    return self.sourceSections[indexPath.section][indexPath.row];
}

-(NSInteger)numberOfSections
{
   return [self.sourceSections count];
}

-(NSArray *)sections
{
    return [self.sourceSections copy];
}

-(NSArray *)sectionIndexTitles
{
    return [self.sectionTitles copy];
}

#pragma - mark Sorting fuction
- (void)sortSearchResultListByDefault
{
    //
    // Sorting Sequence --- follow Ernest's advice
    // 1. sold out or not available, move to bottom
    // 2. hotelRate == 0, move down
    // 3. by distance
    //
    // After sorting, copy result list to searchResultListCopy to store a whole list for filter
    // And Send events for google analytics

    
    self.searchResultList = [NSMutableArray arrayWithArray:[_searchResultList sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        CTEHotelCellData *data1 = obj1;
        CTEHotelCellData *data2 = obj2;

        
        // Step.1--Sold out or unavailable, move to bottom
        if([data1.cteHotel.availabilityErrorCode length] && [data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedSame;
        }
        else if([data1.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedDescending;
        }
        else if([data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedAscending;
        }
        else{               // Step.2--hotelRate==0, move down
            
            double rate1 = [data1.cteHotel.lowestRate doubleValue];
            double rate2 = [data2.cteHotel.lowestRate doubleValue];
            
            if(rate1 == 0 && rate2 > 0){
                return (NSComparisonResult)NSOrderedDescending;
            }
            else if(rate2 == 0 && rate1 > 0){
                return (NSComparisonResult)NSOrderedAscending;
            }
            else{           // Step.3--By distance
                
                if(data1.cteHotel.distance < data2.cteHotel.distance){
                    return (NSComparisonResult)NSOrderedAscending;
                }
                else if(data1.cteHotel.distance > data2.cteHotel.distance){
                    return (NSComparisonResult)NSOrderedDescending;
                }
                return (NSComparisonResult)NSOrderedSame;
            }
        }
        
    }]];
    
     //MOB-21117 Default sort by recommendation score for specific city
     if([self isSpecificCity]){
    
         self.searchResultList = [NSMutableArray arrayWithArray:[_searchResultList sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
             CTEHotelCellData *data1 = obj1;
             CTEHotelCellData *data2 = obj2;
             
             if(data1.cteHotel.recommendationScore > data2.cteHotel.recommendationScore){
             return (NSComparisonResult)NSOrderedAscending;
             }
             else if(data1.cteHotel.recommendationScore < data2.cteHotel.recommendationScore){
             return (NSComparisonResult)NSOrderedDescending;
             }
             return (NSComparisonResult)NSOrderedSame;
         
         }]];
     }
     
    self.searchResultListCopy = [self.searchResultList copy];
    
    [self detectTopFiveRecommendations];
    [self sendAllHotelResultToGA];
}

- (void) sortSearchResultListByDistance
{
    NSUInteger indexOfResultsList = [self.sourceSections indexOfObject:self.searchResultList];
    
    self.searchResultList = [[_searchResultList sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        CTEHotelCellData *data1 = obj1;
        CTEHotelCellData *data2 = obj2;
        
        if([data1.cteHotel.availabilityErrorCode length] && [data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedSame;
        }
        else if([data1.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedDescending;
        }
        else if([data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedAscending;
        }
        else{
            if(data1.cteHotel.distance < data2.cteHotel.distance){
                return (NSComparisonResult)NSOrderedAscending;
            }
            else if ( data1.cteHotel.distance > data2.cteHotel.distance ) {
                return (NSComparisonResult)NSOrderedDescending;
            }
            return (NSComparisonResult)NSOrderedSame;
        }
        
    }] mutableCopy];
    
    self.sourceSections[indexOfResultsList] = self.searchResultList;
    
    [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:[self.sourceSections count]-1 forChangeType:kDataSourceChangeUpdate];
    
    [self sendAllHotelResultToGA];
}

- (void) sortSearchResultListByPreferred
{
    NSUInteger indexOfResultsList = [self.sourceSections indexOfObject:self.searchResultList];
    
    self.searchResultList = [[_searchResultList sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        CTEHotelCellData *data1 = obj1;
        CTEHotelCellData *data2 = obj2;
        
        if([data1.cteHotel.availabilityErrorCode length] && [data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedSame;
        }
        else if([data1.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedDescending;
        }
        else if([data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedAscending;
        }
        else{
            if (data1.cteHotel.companyPreference > data2.cteHotel.companyPreference)
                return NSOrderedAscending;
            else if (data1.cteHotel.companyPreference < data2.cteHotel.companyPreference)
                return NSOrderedDescending;
            return NSOrderedSame;
        }

    }] mutableCopy];
    
    self.sourceSections[indexOfResultsList] = self.searchResultList;
    
    [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:[self.sourceSections count]-1 forChangeType:kDataSourceChangeUpdate];
    
    [self sendAllHotelResultToGA];
}

- (void) sortSearchResultListByStarRating
{
    NSUInteger indexOfResultsList = [self.sourceSections indexOfObject:self.searchResultList];
    
    self.searchResultList = [[_searchResultList sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        CTEHotelCellData *data1 = obj1;
        CTEHotelCellData *data2 = obj2;
        
        if([data1.cteHotel.availabilityErrorCode length] && [data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedSame;
        }
        else if([data1.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedDescending;
        }
        else if([data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedAscending;
        }
        else{
            if(data1.cteHotel.starRating > data2.cteHotel.starRating){
                return (NSComparisonResult)NSOrderedAscending;
            }
            else if(data1.cteHotel.starRating < data2.cteHotel.starRating){
                return (NSComparisonResult)NSOrderedDescending;
            }
            return (NSComparisonResult)NSOrderedSame;
        }
    }] mutableCopy];
    
    self.sourceSections[indexOfResultsList] = self.searchResultList;

    
    [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:[self.sourceSections count]-1 forChangeType:kDataSourceChangeUpdate];
    
    [self sendAllHotelResultToGA];
}

- (void) sortSearchResultListByHotelRate
{

    NSUInteger indexOfResultsList = [self.sourceSections indexOfObject:self.searchResultList];
    
    self.searchResultList = [[_searchResultList sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        CTEHotelCellData *data1 = obj1;
        CTEHotelCellData *data2 = obj2;

        // we want to sort prices numerically, convert a double
        double rate1 = [data1.cteHotel.lowestRate doubleValue];
        double rate2 = [data2.cteHotel.lowestRate doubleValue];
        
        if([data1.cteHotel.availabilityErrorCode length] && [data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedSame;
        }
        else if([data1.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedDescending;
        }
        else if([data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedAscending;
        }
        else{
            if(rate1 > rate2){
                return (NSComparisonResult)NSOrderedAscending;
            }
            else if(rate1 < rate2){
                return (NSComparisonResult)NSOrderedDescending;
            }
            return (NSComparisonResult)NSOrderedSame;
        }
     }] mutableCopy];
    
    self.sourceSections[indexOfResultsList] = self.searchResultList;
        
    [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:[self.sourceSections count]-1 forChangeType:kDataSourceChangeUpdate];
    
    [self sendAllHotelResultToGA];
}

- (void) sortSearchResultListByRecommendationScore
{
    NSUInteger indexOfResultsList = [self.sourceSections indexOfObject:self.searchResultList];
    
    self.searchResultList = [[_searchResultList sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        CTEHotelCellData *data1 = obj1;
        CTEHotelCellData *data2 = obj2;
        
        if([data1.cteHotel.availabilityErrorCode length] && [data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedSame;
        }
        else if([data1.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedDescending;
        }
        else if([data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedAscending;
        }
        else{
            if(data1.cteHotel.recommendationScore > data2.cteHotel.recommendationScore){
                return (NSComparisonResult)NSOrderedAscending;
            }
            else if(data1.cteHotel.recommendationScore < data2.cteHotel.recommendationScore){
                return (NSComparisonResult)NSOrderedDescending;
            }
            return (NSComparisonResult)NSOrderedSame;
        }
    }] mutableCopy];
    
    self.sourceSections[indexOfResultsList] = self.searchResultList;
    
    
    [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:[self.sourceSections count]-1 forChangeType:kDataSourceChangeUpdate];
    
    [self sendAllHotelResultToGA];
}

#pragma mark ---- Filter function
/**
 * Update the FilterDictionary
 */
- (void) updateFilterDict:(NSMutableDictionary *)dict
{
    self.hotelSearchCriteria.filterDict = dict;
}

- (void) defaultFilter
{
    NSMutableDictionary *dict = self.hotelSearchCriteria.filterDict;
    int starRating = [[dict objectForKey:@"Rating"] intValue];
    double distance = [[dict objectForKey:@"Miles"] doubleValue];
    NSString *textContain = [dict objectForKey:@"Text"];
    
    NSMutableArray *ret = [[NSMutableArray alloc] init];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"SELF CONTAINS[cd] %@",textContain];
    for(CTEHotelCellData *hotelcell in self.searchResultListCopy){
        CTEHotel *hotel = hotelcell.cteHotel;
        if((![textContain isEqualToString:@""]) && [pred evaluateWithObject:hotel.propertyName] == NO)
            continue;
        if(hotel.starRating<starRating)
            continue;
        if(distance!=0 && hotel.distance>distance)
            continue;
        [ret addObject:hotelcell];
    }
    self.searchResultList = ret;
}

- (void) filterSearchResultList
{
    NSMutableDictionary *dict = self.hotelSearchCriteria.filterDict;
    int starRating = [[dict objectForKey:@"Rating"] intValue];
    double distance = [[dict objectForKey:@"Miles"] doubleValue];
    NSString *textContain = [dict objectForKey:@"Text"];
    
    NSMutableArray *ret = [[NSMutableArray alloc] init];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"SELF CONTAINS[cd] %@",textContain];
    for(CTEHotelCellData *hotelcell in self.searchResultListCopy){
        CTEHotel *hotel = hotelcell.cteHotel;
        if((![textContain isEqualToString:@""]) && [pred evaluateWithObject:hotel.propertyName] == NO)
            continue;
        if(hotel.starRating<starRating)
            continue;
        if(distance!=0 && hotel.distance>distance)
            continue;
        [ret addObject:hotelcell];
    }
    
    // No need to sorting the filter result because the sequence of "searchResultListCopy" has been sorted
    NSUInteger indexOfResultsList = [self.sourceSections indexOfObject:self.searchResultList];
    self.searchResultList = ret;
    self.sourceSections[indexOfResultsList] = self.searchResultList;
    [self.delegate dataSourceWillChangeData:self];
    [self.delegate dataSource:self didChangeSection:self.hotelListSection atIndex:[self.sourceSections count]-1 forChangeType:kDataSourceChangeUpdate];
	[self.delegate dataSourceDidChangeContent:self];
    
    [self sendAllHotelResultToGA];
}

/**
 * Send the entire list to Google Analytics
 */
- (void)sendAllHotelResultToGA
{
    NSString *allHotelStr = @"{";
    
    for(CTEHotelCellData *cellData in self.searchResultList){
        CTEHotel *tmp = cellData.cteHotel;
        allHotelStr = [allHotelStr stringByAppendingString:[NSString stringWithFormat:@"Property ID:%@",tmp.propertyID]];
        
        if([self.topFiveRecommendations objectForKey:tmp.propertyID]){
            allHotelStr = [allHotelStr stringByAppendingString:[NSString stringWithFormat:@",Recommendation Score:%f",tmp.recommendationScore]];
        }
        
        allHotelStr = [allHotelStr stringByAppendingString:@";"];
    }
    
    allHotelStr = [allHotelStr stringByAppendingString:@"}"];
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Viewed Hotels" eventLabel:allHotelStr eventValue:nil];
}

/**
 * Detect Top 5 Recommendations.
 */
- (void)detectTopFiveRecommendations
{
    NSMutableArray *afterSortArr = [[_searchResultList sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        CTEHotelCellData *data1 = obj1;
        CTEHotelCellData *data2 = obj2;
        
        if([data1.cteHotel.availabilityErrorCode length] && [data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedSame;
        }
        else if([data1.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedDescending;
        }
        else if([data2.cteHotel.availabilityErrorCode length]){
            return (NSComparisonResult)NSOrderedAscending;
        }
        else{
            if(data1.cteHotel.recommendationScore > data2.cteHotel.recommendationScore){
                return (NSComparisonResult)NSOrderedAscending;
            }
            else if(data1.cteHotel.recommendationScore < data2.cteHotel.recommendationScore){
                return (NSComparisonResult)NSOrderedDescending;
            }
            return (NSComparisonResult)NSOrderedSame;
        }
    }] mutableCopy];
    
    int i = 0;
    self.topFiveRecommendations = [[NSMutableDictionary alloc] init];
    for(CTEHotelCellData *cellData in afterSortArr) {
        CTEHotel *tmp = cellData.cteHotel;
        if(i<5){
            [self.topFiveRecommendations setValue:[NSNumber numberWithDouble:tmp.recommendationScore] forKey:tmp.propertyID];
        }
        else{
            break;
        }
        ++i;
    }
}


@end

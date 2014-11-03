//
//  HotelSearchTableViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelSearchTableViewController.h"
#import "SearchHeaderViewCell.h"
#import "HotelsDataSource.h"
#import "SearchCriteriaTableViewCell.h"
#import "SearchCriteriaEditableTableViewCell.h"
#import "SearchDistanceTableViewCell.h"
#import "SearchCriteriaCellData.h"
#import "SearchTableHeaderCellData.h"
#import "HotelSearchMapviewViewController.h"
#import "HotelSearchTableViewCell.h"
#import "HotelSearchFilterViewController.h"
#import "ListViewController.h"
#import "UIView+FindAndResignFirstResponder.h"
#import "CTEDateUtility.h"
#import "DestinationSearchViewController.h"
#import "DatePickerCell.h"
#import "MobileAlertView.h"
#import "SystemConfig.h"
#import "TravelWaitViewController.h"
#import "LoadingSpinnerCellData.h"
#import "LoadingSpinnerTableViewCell.h"
#import "GlobalLocationManager.h"
#import "AnalyticsTracker.h"

@interface HotelSearchTableViewController ()

@property BOOL isSearchViewShowing;
@property (nonatomic, strong) HotelsDataSource *tableData;
@property (nonatomic, strong) NSOperationQueue *imageDownloadQueue;

// keep track which indexPath points to the cell with UIDatePicker
@property (nonatomic, strong) NSIndexPath *datePickerIndexPath;
@property (nonatomic, strong) NSIndexPath *searchDistancePickerIndexPath;

@property (nonatomic, strong) UIBarButtonItem *btnFilter;
@property (nonatomic, strong) UIBarButtonItem *btnSort;
@property (nonatomic, strong) UIBarButtonItem *btnAllResults;
@property (nonatomic, strong) UIBarButtonItem *btnMapView;
@property (nonatomic, strong) UIBarButtonItem *btnVoice;

@property (nonatomic, strong) CTEHotelCellData *selectedHotelCellData;
@property (nonatomic, strong) SearchCriteriaCellData *selectedCriteriaCellData;

// For Filter Traking
@property (nonatomic,strong) NSMutableDictionary *filterSelectedDict;


@end

@implementation HotelSearchTableViewController

#define SORT_BY_DISTANCE			@"Distance"
#define SORT_BY_PREFERRED_VENDORS	@"Preferred"
#define SORT_BY_PRICE				@"Price"
#define SORT_BY_RATING				@"Rating"
#define SORT_BY_RECOMMENDATION      @"Suggested"

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

//Lazy load the operation queue

- (NSOperationQueue *)imageDownloadQueue {
    if (!_imageDownloadQueue) {
        _imageDownloadQueue = [[NSOperationQueue alloc] init];
        _imageDownloadQueue.name = @"Download Queue";
        // Let the OS manage
        _imageDownloadQueue.maxConcurrentOperationCount = 5;
    }
    return _imageDownloadQueue;
}

#pragma mark - UIViewController methods

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    _btnMapView = [self getNavBarButtonWithImage:@"icon_nav_map" withSelector:@selector(showMapView)];
    UIBarButtonItem *fixedItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
    fixedItem.width = 20.0f; // or whatever you want

    _btnVoice = [self getNavBarButtonWithImage:@"icon_nav_voice" withSelector:@selector(showVoiceSearch)];
    // disable mapview until some results are loaded.
    _btnMapView.enabled = NO;
    _btnSort.enabled = NO;
    _btnVoice.enabled = NO;
    [self.navigationItem setRightBarButtonItems:[NSArray arrayWithObjects:_btnMapView,fixedItem,_btnVoice, nil]];
   
    self.title = @"Hotels";
    self.isSearchViewShowing = NO;
    self.tableData = [[HotelsDataSource alloc] init];

    // Enable location services tracking, but dont get fire the search.
    if (![ExSystem sharedInstance].isGovernment)
    {
        [GlobalLocationManager startTrackingSignificantLocationUpdates];
        // default value for BOOL property is NO,
        [self startListeningToCurrentLocationUpdates];
        [self startListeningToCurrentLocationAuthorization];
    }
    
     __weak typeof(self) weakSelf = self;
    [self.tableData setOnSearchError:^(NSString *error) {
    
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:@"OOPS" message:error delegate:nil cancelButtonTitle:@"Close" otherButtonTitles:nil, nil];
        
        if (weakSelf.isViewLoaded && weakSelf.view.window) {
            // viewController is visible
            [alert show];
        }
        
    }];
    
    if ([SystemConfig getSingleton] == nil)
    {
        [[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_SYSTEM_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];
    }

    self.tableData.delegate = self;
//    [self.tableData loadContent];
    // Disabled Waitview until we have a clear UX design on what waitviews to show.
    [self.tableData setHideWaitView:^{
        [TravelWaitViewController hideAnimated:YES withCompletionBlock:nil];
    }];
    [self setupToolbar];

    //Set up Filter
    if(self.filterSelectedDict == nil){
        self.filterSelectedDict = [[NSMutableDictionary alloc] init];
        [self.filterSelectedDict setObject:@"0" forKey:@"Rating"];
        [self.filterSelectedDict setObject:@"0" forKey:@"Miles"];
        [self.filterSelectedDict setObject:@"" forKey:@"Text"];
    }
    
    [self.tableData updateFilterDict:self.filterSelectedDict];
    
    self.btnSort.enabled = NO;
    [self.tableData setAfterDoneSearch:^{
        weakSelf.btnSort.enabled = YES;
        weakSelf.btnMapView.enabled = YES;
        weakSelf.btnFilter.enabled = YES;
    }];
    
    [self.tableView registerNib:[UINib nibWithNibName:@"HotelSearchTableViewCell" bundle:nil] forCellReuseIdentifier:@"HotelSearchTableViewCell"];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tableListWasTapped:)];
    tap.cancelsTouchesInView = NO;
    [self.tableView addGestureRecognizer:tap];
    

        // if hotels search near me is not enabled then show the search criteria.
    if (!self.isSearchHotelsNearMeEnabled) {
        [self toggleSearchCriteriaSection];
    }
    
    [AnalyticsTracker initializeScreenName:@"Search Results/Criteria"];
}


-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:NO];
   
    // Title should be set by the instantiating method
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    if ([self.navigationController.viewControllers indexOfObject:self]==NSNotFound) {
        // MOB-21116 - We only want to hide the wait window if we are navigating back to the home screen
        [TravelWaitViewController hideAnimated:YES withCompletionBlock:nil];
    }
    [self.imageDownloadQueue setSuspended:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)tableListWasTapped:(UITapGestureRecognizer*)gesture
{
    [self.tableView endEditing:YES];
}


#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
   return [self.tableData numberOfSections];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section
    return [self.tableData.sections[section] count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    AbstractTableViewCellData *cellData = [self.tableData itemAtIndexPath:indexPath];
    return cellData.cellHeight;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    // get the first section data
    
    AbstractTableViewCellData *dataObject = [self.tableData itemAtIndexPath:indexPath];
    
    if ([dataObject isKindOfClass:[SearchTableHeaderCellData class]]) {
        
        SearchTableHeaderCellData *cellData = (SearchTableHeaderCellData *)dataObject;
        SearchHeaderViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier forIndexPath:indexPath];
        [cell setCellData:cellData];
        return cell;
    }
    else if ([dataObject isKindOfClass:[SearchCriteriaCellData class]]) {
        
        SearchCriteriaCellData *cellData = (SearchCriteriaCellData *)dataObject;
        if ([cellData.cellIdentifier isEqualToString:@"distancePickerCell"]) {
            SearchDistanceTableViewCell *distancePickerCell = [tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier forIndexPath:indexPath];
            [distancePickerCell.distancePicker selectRow:3 inComponent:0 animated:NO];
            [distancePickerCell setHotelSearchDistanceDidChanged:^(NSString *distanceValue){
                [self updateHotelSearchDistance:distanceValue];
            }];
            return distancePickerCell;
        }
        else if ([cellData.cellIdentifier isEqualToString:@"datePickerCell"]) {
             DatePickerCell *cell = [tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier forIndexPath:indexPath];
            [cell setCellData:cellData];
            // Set the call back block so we can update our dates when user changes the dates.
            [cell setOnDateSelected:^(NSDate *date) {
                [self dateAction:date title:cellData.cellName];
            }];
            return cell;
        }
        else if ([cellData.cellIdentifier isEqualToString:@"SearchCriteriaCell"]){
            SearchCriteriaTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier forIndexPath:indexPath];
            [cell setCellData:cellData];
            return cell;
        }

        else if ([cellData.cellIdentifier isEqualToString:@"SearchCriteriaEditableCell"]){
            SearchCriteriaEditableTableViewCell *editableCell = [tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier forIndexPath:indexPath];
            [editableCell setCellData:cellData];
            [editableCell setOnTextChanged:^(NSString *textString) {
                [self.tableData updateHotelNameCriteria:textString];
                [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Name Containing" eventLabel:nil eventValue:nil];
            }];
            return editableCell;
        }

    }
    else if ([dataObject isKindOfClass:[LoadingSpinnerCellData class]])
    {
        // return the table view cell
        LoadingSpinnerTableViewCell *spinnerCell = [tableView dequeueReusableCellWithIdentifier:dataObject.cellIdentifier forIndexPath:indexPath];
        [spinnerCell setCellData:(LoadingSpinnerCellData *)dataObject];
        return spinnerCell;
        
    }
    else {
        CTEHotelCellData *cellData =  (CTEHotelCellData *)dataObject;
        
        HotelSearchTableViewCell *hotelListCell = [self.tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier forIndexPath:indexPath];
        
        if (hotelListCell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelSearchTableViewCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[HotelSearchTableViewCell class]])
                    hotelListCell = (HotelSearchTableViewCell *)oneObject;
        }
        
        [hotelListCell setCellData:cellData indexPath:indexPath];
        
        [hotelListCell displayCellAsEnabled:self.tableData.searchDone && hotelListCell.isCellEnabled ];
        
         return hotelListCell;
    }
    return nil;
}


-(UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if ([self.tableData.sectionIndexTitles count] > section) {
        NSString *sectionTitle = self.tableData.sectionIndexTitles[section];
        if ([sectionTitle lengthIgnoreWhitespace]) {
            return [self getHeaderView:sectionTitle];
        }
    }
    return nil;
}

-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if ([self.tableData.sectionIndexTitles count] > section) {
        NSString *sectionTitle = self.tableData.sectionIndexTitles[section];
        if ([sectionTitle length]) {
            return [self getHeaderView:sectionTitle].frame.size.height;
        }
    }
    return 0;
}


#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    //  Get the correspoinding cell data
    id dataObj = self.tableData.sections[indexPath.section][indexPath.row];
    
    if ([dataObj isKindOfClass:[SearchTableHeaderCellData class]]) {
        [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Search Input" eventLabel:nil eventValue:nil];
        [self toggleSearchCriteriaSection];
    }
    else if ([dataObj isKindOfClass:[SearchCriteriaCellData class]])
    {
        self.selectedCriteriaCellData = (SearchCriteriaCellData *)dataObj;
        // TODO : Handle other types of cells
        if(self.selectedCriteriaCellData.cellType == kDatePicker){
            [self toggleDatePickerAtIndexPath:indexPath];
        }
        else if (self.selectedCriteriaCellData.cellType == kPickerView){
            [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Hotel Within" eventLabel:nil eventValue:nil];
            [self toggleSearchDistancePickerAtIndexPath:indexPath];
        }

        else if ([self.selectedCriteriaCellData.cellIdentifier isEqualToString:@"SearchCriteriaEditableCell"]){
            self.selectedCriteriaCellData.subTitle = @"";
         }
        // There is only one location list in this table
        else if (self.selectedCriteriaCellData.cellType == kLocationList) {
            // show destination search ui
            [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Destination" eventLabel:nil eventValue:nil];
            [self performSegueWithIdentifier:@"destinationSearch" sender:self];
        }

        else
            return;
    }
    else if ([dataObj isKindOfClass:[CTEHotelCellData class]]){
        _selectedHotelCellData = (CTEHotelCellData *)dataObj;
        [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Choose Hotel" eventLabel:[_selectedHotelCellData getCTEHotel].propertyID eventValue:nil];
        [self performSegueWithIdentifier:@"hotelRoomsList" sender:self];
    }
    
    [self.tableView deselectRowAtIndexPath:indexPath animated:YES];
    

 }

#pragma mark - Date Picker

/*!
 Inserts or hides a date picker spindel below the check-in or check-out date fields.
 */
-(void)toggleDatePickerAtIndexPath:(NSIndexPath *)indexPath
{
    BOOL isOldPickerBeforeCurrent = NO;   // indicates if the date picker is below "indexPath", help us determine which row to reveal
    if (self.datePickerIndexPath != nil)
    {
        isOldPickerBeforeCurrent = self.datePickerIndexPath.row < indexPath.row;
    }
    
    
    // TODO : Copy/Paste logic from standalone. Refactor this to work in tandem with Datasource 
    // ==============
    [self.tableView beginUpdates];
    
    // if distance picker is showing, remove it.
    if (self.searchDistancePickerIndexPath != nil) {
        NSArray *distancePickerIndexPaths = @[[NSIndexPath indexPathForRow:self.searchDistancePickerIndexPath.row + 1 inSection:self.searchDistancePickerIndexPath.section]];
        [self deleteDistancePickerAtIndexPath:distancePickerIndexPaths[0]];
        self.searchDistancePickerIndexPath = nil;
    }
    
    if (self.datePickerIndexPath == nil) {  // Datepicker is not shown
        // insert the datepicker in the next row.
        NSArray *newIndexPaths = @[[NSIndexPath indexPathForRow:indexPath.row + 1 inSection:indexPath.section]];
        // add a datepicker row in table data
        [self.tableData insertDatePicker:indexPath];
        [self.tableView insertRowsAtIndexPaths:newIndexPaths
                              withRowAnimation:UITableViewRowAnimationFade];
        // Save the index path
        self.datePickerIndexPath = indexPath;
        
    }
    else    // datepicker is already showing
    {
        // delete cell for date picker
        [self deleteDatePickerAtIndexPath:indexPath];
        
        // user clicked some other row, so show datepicker at other row
        if ( self.datePickerIndexPath != nil )
        {
            NSArray *newIndexPaths = nil;
            // insert the datepicker in the next row.
            if (isOldPickerBeforeCurrent) {
                newIndexPaths = @[[NSIndexPath indexPathForRow:indexPath.row inSection:indexPath.section]];
                [self.tableData insertDatePicker:[NSIndexPath indexPathForRow:indexPath.row - 1 inSection:indexPath.section]];
                self.datePickerIndexPath = [NSIndexPath indexPathForRow:indexPath.row - 1 inSection:indexPath.section];
            }
            else
            {
                newIndexPaths = @[[NSIndexPath indexPathForRow:indexPath.row + 1 inSection:indexPath.section]];
                [self.tableData insertDatePicker:[NSIndexPath indexPathForRow:indexPath.row inSection:indexPath.section]];
                self.datePickerIndexPath = indexPath;
            }
            NSLog(@"index.row = %ld", (long)indexPath.row );
            // add a datepicker row in table data
            
            [self.tableView insertRowsAtIndexPaths:newIndexPaths
                                  withRowAnimation:UITableViewRowAnimationFade];
            // Save the index path
        }
        
    }
    
    [self.tableView endUpdates];
}

-(void)deleteDatePickerAtIndexPath:(NSIndexPath *)indexPath
{
    NSArray *oldIndexPaths = @[[NSIndexPath indexPathForRow:self.datePickerIndexPath.row + 1 inSection:self.datePickerIndexPath.section]];
    
    // already showing the datepicker so remove the date picker in next row.
    [self.tableView deleteRowsAtIndexPaths:oldIndexPaths
                          withRowAnimation:UITableViewRowAnimationFade];
    
    [self.tableData removeDatePicker:self.datePickerIndexPath];
    
    if ( [self.datePickerIndexPath isEqual:indexPath])
    {
        // set it to nil since user clicked the same row
        self.datePickerIndexPath = nil;
        
    }
}

#pragma mark - Distance picker
/*!
 Inserts or hides a distance picker below hotels within row.
 */
 // TODO: This might merge with toggleDatePickerAtIndexPath to have a unified method
-(void)toggleSearchDistancePickerAtIndexPath:(NSIndexPath *)indexPath
{
    BOOL isOldPickerBeforeCurrent = NO;   // indicates if the date picker is below "indexPath", help us determine which row to reveal
    if (self.searchDistancePickerIndexPath != nil) {
        isOldPickerBeforeCurrent = self.searchDistancePickerIndexPath.row < indexPath.row;
    }
    
    [self.tableView beginUpdates];
    
    if (self.searchDistancePickerIndexPath == nil) {  // picker is not shown
        BOOL isDatePickerShowing = NO;
        // if datePicker is showing, remove it
        if (self.datePickerIndexPath != nil) {
            isDatePickerShowing = TRUE;
            NSArray *datePickerIndexPaths = @[[NSIndexPath indexPathForRow:self.datePickerIndexPath.row + 1 inSection:self.datePickerIndexPath.section]];
            [self deleteDatePickerAtIndexPath:datePickerIndexPaths[0]];
            self.datePickerIndexPath = nil;
        }
        // insert the picker in the next row.
        if (isDatePickerShowing) {
            NSArray *newIndexPaths = @[[NSIndexPath indexPathForRow:indexPath.row inSection:indexPath.section]];
            // add a picker row in table data
            [self.tableData insertSearchDistancePicker:[NSIndexPath indexPathForRow:indexPath.row - 1 inSection:indexPath.section]];
            [self.tableView insertRowsAtIndexPaths:newIndexPaths
                                  withRowAnimation:UITableViewRowAnimationFade];
            self.searchDistancePickerIndexPath = [NSIndexPath indexPathForRow:indexPath.row - 1 inSection:indexPath.section];
        } else {
        NSArray *newIndexPaths = @[[NSIndexPath indexPathForRow:indexPath.row + 1 inSection:indexPath.section]];
        // add a picker row in table data
        [self.tableData insertSearchDistancePicker:indexPath];
        [self.tableView insertRowsAtIndexPaths:newIndexPaths
                              withRowAnimation:UITableViewRowAnimationFade];
        // Save the index path
        self.searchDistancePickerIndexPath = indexPath;
        }
        
    }
    else    // datepicker is already showing
    {
        // delete picker
        [self deleteDistancePickerAtIndexPath:indexPath];
        if (self.searchDistancePickerIndexPath != nil)
        {
             // user clicked some other row, so show datepicker at other row
            NSArray *newIndexPaths = nil;
            // insert the datepicker in the next row.
            if (isOldPickerBeforeCurrent) {
                newIndexPaths = @[[NSIndexPath indexPathForRow:indexPath.row inSection:indexPath.section]];
                [self.tableData insertSearchDistancePicker:[NSIndexPath indexPathForRow:indexPath.row - 1 inSection:indexPath.section]];
                self.searchDistancePickerIndexPath = [NSIndexPath indexPathForRow:indexPath.row - 1 inSection:indexPath.section];
            }
            else
            {
                newIndexPaths = @[[NSIndexPath indexPathForRow:indexPath.row + 1 inSection:indexPath.section]];
                [self.tableData insertSearchDistancePicker:[NSIndexPath indexPathForRow:indexPath.row inSection:indexPath.section]];
                self.searchDistancePickerIndexPath = indexPath;
            }
            NSLog(@"index.row = %ld", (long)indexPath.row );
            [self.tableView insertRowsAtIndexPaths:newIndexPaths
                                  withRowAnimation:UITableViewRowAnimationFade];
        }
        
    }
    
    [self.tableView endUpdates];
    
}
// TODO: Merge with deleteDatePickerAtIndexPath
-(void)deleteDistancePickerAtIndexPath:(NSIndexPath *)indexPath
{
    NSArray *oldIndexPaths = @[[NSIndexPath indexPathForRow:self.searchDistancePickerIndexPath.row + 1 inSection:self.searchDistancePickerIndexPath.section]];
    
    // already showing the datepicker so remove the date picker in next row.
    [self.tableView deleteRowsAtIndexPaths:oldIndexPaths
                          withRowAnimation:UITableViewRowAnimationFade];
    
    [self.tableData removeDatePicker:self.searchDistancePickerIndexPath];
    
    if ( [self.searchDistancePickerIndexPath isEqual:indexPath])
    {
        // set it to nil since user clicked the same row
        self.searchDistancePickerIndexPath = nil;
    }
}

-(void)updateHotelSearchDistance:(NSString *)distanceValue
{
    NSIndexPath *targetedCellIndexPath = nil;

    // inline date picker: update the cell's date "above" the date picker cell
    targetedCellIndexPath = [NSIndexPath indexPathForRow:self.searchDistancePickerIndexPath.row inSection:self.searchDistancePickerIndexPath.section];
    
    SearchCriteriaTableViewCell *cell = (SearchCriteriaTableViewCell*)[self.tableView cellForRowAtIndexPath:targetedCellIndexPath];

    cell.lblSubTitle.text = distanceValue;
}

#pragma mark - Generic class methods
/*!
 Creates a Navbar buttons with given image and selector.
 */
-(UIBarButtonItem *)getNavBarButtonWithImage:(NSString *)imgName withSelector:(SEL)selectorName
{
    UIButton* mbtn =[UIButton buttonWithType:UIButtonTypeCustom];
    UIImage* mImage = [UIImage imageNamed:imgName];
    [mbtn addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    [mbtn setImage:mImage forState:UIControlStateNormal];
    mbtn.frame = CGRectMake(0, 0, mImage.size.width, mImage.size.height);
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc]initWithCustomView:mbtn];
    
    return menuButton;
    
}

/*
 Action method for Search button in Search Criteria
 */
-(void)searchHotels
{
    // check if criteria is valid
    // If Criteria is invalid then mostly it will be hotel location or it might be invalid hotels within string.
    // TODO : Add validation for hotels within also.
    [self.tableData updateHotelSearchCriteria];
    if (![self.tableData isSearchCriteriaValid]) {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:@"OOPS" message:@"Invalid location. Please select a location and press search" delegate:nil cancelButtonTitle:@"Close" otherButtonTitles:nil, nil];
        [alert show];
        return ;
    }
	
    DLog(@"Searching for Hotels now ...");
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Search" eventLabel:nil eventValue:nil];
    // Collapse any expanded rows in the search criteria before calling updateSearchCriteria.
    // since updateSearchCriteria doesnt expect any picker rows.
    if (self.datePickerIndexPath != nil) {
        [self toggleDatePickerAtIndexPath:self.datePickerIndexPath];
    }else if (self.searchDistancePickerIndexPath != nil) {
         [self toggleSearchDistancePickerAtIndexPath:self.searchDistancePickerIndexPath];
    }
    
    [self toggleSearchCriteriaSection];
    _btnMapView.enabled = NO;
    _btnSort.enabled = NO;
    _btnFilter.enabled = NO;
    
    //
    // Reset Filter
    [self.filterSelectedDict setObject:@"0" forKey:@"Rating"];
    [self.filterSelectedDict setObject:@"0" forKey:@"Miles"];
    [self.filterSelectedDict setObject:@"" forKey:@"Text"];
    [self.tableData updateFilterDict:self.filterSelectedDict];
    
 
    [TravelWaitViewController showTransparentWithText:[Localizer getLocalizedText:@"Searching for hotels"] animated:YES];
      // Make the search call
    [self.tableData searchHotels];
    id dataObject = [self.tableData itemAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0]];
    
    // find the city name where searching for hotels
    NSString *location = nil;
    if ([dataObject isKindOfClass:[SearchTableHeaderCellData class]]) {
        location = ((SearchTableHeaderCellData*)dataObject).location;
    }
 
}

- (UIBarButtonItem*)makeHotelCountButton:(int)resultCount
{
	const int buttonWidth = 200;
	const int buttonHeight = 32;
	
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	lblText.numberOfLines = 1;
	lblText.lineBreakMode = NSLineBreakByTruncatingTail;
	lblText.textAlignment = NSTextAlignmentCenter;
    
    lblText.text = [NSString stringWithFormat:@"%d %@", resultCount, [Localizer getLocalizedText:@"Results"]];
    
    if(![ExSystem is7Plus])
	{
        // Only change the results text color if using iOS6
        [lblText setBackgroundColor:[UIColor clearColor]];
        [lblText setTextColor:[UIColor whiteColor]];
    }
    [lblText setFont:[UIFont systemFontOfSize:11]];
    //	[lblText setShadowColor:[UIColor grayColor]];
    //	[lblText setShadowOffset:CGSizeMake(1, 1)];
	[cv addSubview:lblText];
	
	UIBarButtonItem* btnResultCount = [[UIBarButtonItem alloc] initWithCustomView:cv];
	
	
	return btnResultCount;
}

#pragma mark -  Toolbar and navbar
- (void)setupToolbar
{
    if (!self.btnFilter)
    {
        self.btnFilter = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Filter"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonFilterPressed:)];
        [self.btnFilter setEnabled:NO];
    }
    
    if (!self.btnSort)
        self.btnSort = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Sort"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSortPressed:)];
    
    // number of hotels
    int hotelCount = [self.tableData.hotelListSection numberOfObjects];
    self.btnAllResults = [self makeHotelCountButton:hotelCount];
    // number of Hotels
    
    UIBarButtonItem *padding = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
    [self setToolbarItems:@[padding, self.btnFilter, padding, self.btnAllResults, padding, self.btnSort, padding]];
    [self.navigationController setToolbarHidden:NO];
}

// TODO: Toggle the toolbar items between filter cirtera and search button
- (void)showToolBar
{
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(searchHotels)];
    
    [self.ivSearchView addGestureRecognizer:tapGesture];
    self.ivSearchView.hidden = NO;
    
    UIBarButtonItem *leftPadding = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
    leftPadding.width = -16;
    
    [self setToolbarItems:@[leftPadding,[[UIBarButtonItem alloc] initWithCustomView:self.ivSearchView]]];
    
    [UIView animateWithDuration:2.0
                     animations:^{
                         [self.navigationController setToolbarHidden:NO animated:YES];
                     }
                     completion:^(BOOL finished){
                         // whatever
                     }];
}


-(UIView*)getHeaderView:(NSString *)sectionTitle
{
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.tableView.bounds.size.width, 40)];

    [headerView setBackgroundColor:[UIColor hotelsListSectionHeaderBarColor]];
    UILabel *headerTitle = [[UILabel alloc] initWithFrame:CGRectMake(15, 8, 200, 25)];
    [headerTitle setText:sectionTitle];
    [headerTitle setTextColor:[UIColor colorWithRed:0.0/255.0 green:120.0/255.0 blue:200.0/255.0 alpha:1.0]];
    [headerTitle setFont:[UIFont fontWithName:@"Helvetica Neue" size:16.0]];
    [headerTitle sizeToFit];
    [headerTitle setTextAlignment:NSTextAlignmentLeft];
    
    [headerView addSubview:headerTitle];
    return headerView;
}

/*!
 Adds a close button to the navbar when Search criteria section is expanded.
 */
-(void)addCloseButtun
{
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:@"Close"
                                                                   style:UIBarButtonItemStyleDone
                                                                  target:self
                                                                  action:@selector(toggleSearchCriteriaSection)];
    [self.navigationItem setLeftBarButtonItem:doneButton];
    
}

/*!
 Removes the close button from nav bar when search criteria section is hidden
 */
-(void)removeCloseButtun
{
    [self.navigationItem setLeftBarButtonItem:nil];
    
}


/*! Show/Hide Search Criteria section for hotel search
 */
- (void)toggleSearchCriteriaSection
{    
    // Always insert at section zero
    NSIndexPath *path = [NSIndexPath indexPathForRow:0 inSection:0];
    
    if (self.isSearchViewShowing) {
        // remove section
        self.isSearchViewShowing = NO;
        self.ivSearchView.hidden = YES;
        UIBarButtonItem *fixedItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
        fixedItem.width = 20.0f; // or whatever you want
        [self.navigationItem setRightBarButtonItems:[NSArray arrayWithObjects:_btnMapView,fixedItem,_btnVoice, nil]];
        [self removeCloseButtun];
        
        
        
        // if the user does not close the picker view and tap on close button, when tapping on the searchTableViewHeader cell to come back,
        // click on the cell that showing picker, it will delete the next cell.
        // another way to do it just delete the picer cell when CLOSE button is tapped.
        self.datePickerIndexPath = nil;
        self.searchDistancePickerIndexPath = nil;
        self.title = @"Hotels";
    }
    else
    {
        self.isSearchViewShowing = YES;
        [self.navigationItem setRightBarButtonItems:[NSArray arrayWithObjects:_btnVoice, nil]];
        [self showToolBar];
        [self addCloseButtun];
        self.title = @"Search";
        
    }
    
    [self.tableData toggleSearchCriteriaSection];
    
   [self.tableView scrollToRowAtIndexPath:path
                          atScrollPosition:UITableViewScrollPositionBottom
                                  animated:YES];
    
}

#pragma mark IBAction methods

/*!
 * Called while user is typing. Updates the hotel names criteria with the input string
 */
-(void)setHotelNameCriteria:(NSString *)hotelNameCriteria
{
    
}

/*! User chose to change the date by changing the values inside the UIDatePicker cell.
 @param sender The sender for this action: date.
 */
- (IBAction)dateAction:(id)sender title:(NSString *)cellName
{
// update cell data source to refresh the table view
    NSDate *targetedDate = sender;
    [self.tableData updateDate:targetedDate atIndexPath:self.datePickerIndexPath];

}

- (IBAction)buttonFilterPressed:(id)sender
{    
    HotelSearchFilterViewController *vc = [[HotelSearchFilterViewController alloc] init];
    vc.selectedIndexDict = self.filterSelectedDict;
    
    [vc setFilterTracking:^(NSMutableDictionary *dict){
        self.filterSelectedDict = dict;
        [self.tableData updateFilterDict:dict];
        [self.tableData filterSearchResultList];
    }];
    
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Filter" eventLabel:nil eventValue:nil];
    [self presentViewController:vc animated:YES completion:nil];
}

- (NSArray *) getListItems
{
    NSArray *items =@[@"Distance", @"Preferred", @"Price", @"Rating",@"Suggested"];
    NSMutableArray* result = [[NSMutableArray alloc] initWithCapacity:5];
    for (NSString *item in items)
    {
        ListItem * li = [[ListItem alloc] init];
        li.liName = item;
        [result addObject:li];
    }
    return result;
}

- (IBAction)buttonSortPressed:(id)sender
{
    ListViewController *vc = [[ListViewController alloc] initWithNibName:@"ListView" bundle:nil];
    
    vc.dataSourceArray = [self getListItems];
    if([self.tableData isSpecificCity]){
        self.btnSort.tag = 4;
    }
    vc.defaultSelectedIdxPath = [NSIndexPath indexPathForRow:self.btnSort.tag inSection:0];
    vc.delegate = self;
    
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Sort" eventLabel:nil eventValue:nil];
    [self presentViewController:vc animated:YES completion:nil];
}

#pragma mark - show self

+(void) showHotelsNearMe:(UINavigationController *)navi
{
    HotelSearchTableViewController *hotelsNearMeTableViewController =
    [[UIStoryboard storyboardWithName:[@"HotelBookingFlow" storyboardName] bundle:nil] instantiateInitialViewController];
    
    hotelsNearMeTableViewController.title = @"Hotels";
    // TODO : customize other stuff here like voice flags etc.
    //    hotelsNearMeTableViewController.category = EVA_HOTELS;
    [navi pushViewController:hotelsNearMeTableViewController animated:YES];
}

#pragma mark - AbstractDataSourceDelegates

-(void)dataSourceWillChangeData:(AbstractDataSource *)dataSource
{
    [self.tableView beginUpdates];
}

-(void)dataSource:(AbstractDataSource *)dataSource didChangeSection:(id<AbstractDataSourceSectionInfo>)sectionInfo atIndex:(NSUInteger)sectionIndex forChangeType:(DataSourceChangeType)type
{
    
    switch(type)
    {
        case kDataSourceChangeInsert:
            [self.tableView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case kDataSourceChangeDelete:
            [self.tableView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case kDataSourceChangeUpdate:
            [self.tableView reloadSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;

    }

}

- (void)dataSource:(AbstractDataSource *)dataSource didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(DataSourceChangeType)type newIndexPath:(NSIndexPath *)newIndexPath
{
    switch(type)
    {
            
        case kDataSourceChangeInsert:
            [self.tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case kDataSourceChangeDelete:
            [self.tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case kDataSourceChangeUpdate:
             [self.tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeMove:
            [self.tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [self.tableView insertRowsAtIndexPaths:@[newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }

}

- (void)dataSourceDidChangeContent:(AbstractDataSource *)dataSource
{
    [self.tableView endUpdates];
    
    if (!self.isSearchViewShowing) {
        [self setupToolbar];
    }
    
}

#pragma mark - ImageDownloaderOperationDelegate 
-(void)ImageDownloaderOperationDidFinish:(ImageDownloaderOperation *)downloader
{
    // 1: Check for the indexPath of the operation, whether it is a download, or filtration.
    NSIndexPath *indexPath = downloader.indexPathInTableView;
    // 2: TODO : This is a brute for logic and is very time consuming
    // Create a dict for indexpath and celldata so datasource can return immediately. 

    // 3: Update UI.
    // Check if the index path is still valid
    // If user fired a search while loading images then the current search is cleared
    // TODO : Pause all images downloads if user fires a search while current search is still loading.
    if([self.tableView.indexPathsForVisibleRows containsObject:indexPath])
    {
        [self.tableView beginUpdates];
        [self.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
        [self.tableView endUpdates];
    }
    
 
}

-(void)showMapView
{
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Map" eventLabel:nil eventValue:nil];
    [self performSegueWithIdentifier:@"showmapview" sender:self];
}

-(void)showVoiceSearch
{
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Microphone" eventLabel:nil eventValue:nil];
}

#pragma mark - segue methods

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if([segue.identifier isEqualToString:@"showmapview"])
    {
        // Set the mapviewidentifier
        UINavigationController *nav = segue.destinationViewController;
        
        HotelSearchMapviewViewController *destination = (HotelSearchMapviewViewController *)nav.topViewController;
#warning remove the hardcoded stuff later and expose a way to get hotellist
        destination.hotelList = [self.tableData.hotelListSection objects];
    }
    if ([segue.identifier isEqualToString:@"destinationSearch"]) {
        UINavigationController *nav = segue.destinationViewController;
        // Set the call back block
        // TODO : Send the current selected location to the destination controller
        DestinationSearchViewController *destination = (DestinationSearchViewController *)nav.topViewController;

        [destination setOnLocationSelected:^(CTELocation  *cteLocation) {
            // Call the method to update the data table
            [self updateDestination:cteLocation];
        }];
        
        if (self.selectedCriteriaCellData != nil) {
            CTELocation *location = (CTELocation *)self.selectedCriteriaCellData.keyValue[@"cellvalue"];
            if (location != nil) {
                [destination setSelectedLocation:location];
            }
        }
    }
    if ([segue.identifier isEqualToString:@"hotelRoomsList"]) {
        HotelRoomsListTableViewController *roomsListViewController = segue.destinationViewController;
        roomsListViewController.hotelCellData = self.selectedHotelCellData;
    }
}

-(void)updateDestination:(CTELocation *)cteLocation
{
    DLog(@"Location updated :%@ ", cteLocation.location );
    [self.tableData updateDestination:cteLocation];
}

-(BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
    if([identifier isEqualToString:@"showmapview"] || [identifier isEqualToString:@"destinationSearch"])
        return YES;
    
    return NO;
}

#pragma mark Current Location tracking Methods
-(void) startListeningToCurrentLocationUpdates
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter addObserver:self selector:@selector(receivedCurrentLocationUpdate:) name:CURRENT_LOCATION_UPDATE object:nil];
    [defaultCenter addObserver:self selector:@selector(receivedLocationUpdateFailed:) name:CURRENT_LOCATION_FAILED object:nil];
}

-(void) stopListeningToCurrentLocationUpdates
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter removeObserver:self name:CURRENT_LOCATION_UPDATE object:nil];
    [defaultCenter removeObserver:self name:CURRENT_LOCATION_FAILED object:nil];
}

-(void) receivedCurrentLocationUpdate:(NSNotification*)notification
{
    if (self.tableData != nil)
    {
        if (self.isSearchHotelsNearMeEnabled) {
                [self.tableData searchHotelsNearMe];
        } else
        {
            CTELocation *mylocation = [[CTELocation alloc] init];
            mylocation.location = [@"Current Location" localize];
            CLLocation *currentLocation = [GlobalLocationManager sharedInstance].currentLocation;
            mylocation.longitude = currentLocation.coordinate.longitude;
            mylocation.latitude = currentLocation.coordinate.latitude;

            [self.tableData updateDestination:mylocation];
        }
        // Start new search.

    }
}

-(void) receivedLocationUpdateFailed:(NSNotification*)notification
{
    if (self.tableData != nil)
    {
        // TODO: Either location update failed or user denied location, so current location cannot be used.
    }
}

// get user choice
-(void) startListeningToCurrentLocationAuthorization
{
    NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
    [defaultCenter addObserver:self selector:@selector(receivedLocationUpdateFailed:) name:LOCATION_AUTHORIZATION_NOT_ALLOWED object:nil];
}

// get user choice
-(void) stopListeningToCurrentLocationAuthorization
{
    NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
    [defaultCenter removeObserver:self name:LOCATION_AUTHORIZATION_NOT_ALLOWED object:nil];
}



#pragma mark - memory management

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
    self.tableData.delegate = nil;
	self.tableData = nil;
	[self.imageDownloadQueue cancelAllOperations];
    // Disable locaiton tracking
    [GlobalLocationManager stopTrackingSignificantLocationUpdates];
    [self stopListeningToCurrentLocationUpdates];
    [self stopListeningToCurrentLocationAuthorization];
}


- (void)dealloc
{
    [TravelWaitViewController hideAnimated:YES withCompletionBlock:nil];
	self.tableData.delegate = nil;
    self.tableData = nil;
    [GlobalLocationManager stopTrackingSignificantLocationUpdates];
    [self stopListeningToCurrentLocationUpdates];
    [self stopListeningToCurrentLocationAuthorization];
}

#pragma - mark OptionsSelectDelegate
- (void)optionSelectedAtIndex:(NSInteger)row withIdentifier:(NSObject *)identifier
{
    // btn tag is used to save last selected index-row.
    NSString *sortingRules = (NSString *)identifier;
    if ([sortingRules isEqualToString:SORT_BY_DISTANCE]) {
        self.btnSort.tag = 0;
        [self.tableData sortSearchResultListByDistance];
    }
    else if ([sortingRules isEqualToString:SORT_BY_PREFERRED_VENDORS])
    {
        self.btnSort.tag = 1;
        [self.tableData sortSearchResultListByPreferred];
    }
    else if ([sortingRules isEqualToString:SORT_BY_PRICE])
    {
        self.btnSort.tag = 2;
        [self.tableData sortSearchResultListByHotelRate];
    }
    else if ([sortingRules isEqualToString:SORT_BY_RATING])
    {
        self.btnSort.tag = 3;
        [self.tableData sortSearchResultListByStarRating];
    }
    else if([sortingRules isEqualToString:SORT_BY_RECOMMENDATION]){
        self.btnSort.tag = 4;
        [self.tableData sortSearchResultListByRecommendationScore];
    }

}


@end

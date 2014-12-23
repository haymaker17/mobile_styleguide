//
//  DestinationSearchDataSource.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "DestinationSearchDataSource.h"
#import "DestinationSearchCellData.h"
#import "LocationSearchCellData.h"
//#import "CTENetworkSettings.h"
#import "CTELocation.h"
#import "CTELocationSearch.h"
#import "ConcreteDataSourceSectionInfo.h"

@interface DestinationSearchDataSource ()

@property (nonatomic, strong) NSMutableArray *locationCells;
@property (nonatomic, strong) NSMutableArray *officeLocationCells;

@property (nonatomic, strong) ConcreteDataSourceSectionInfo *locationsSectionInfo;
@property (nonatomic, strong) ConcreteDataSourceSectionInfo *currentLocationsSectionInfo;
@property (nonatomic, strong) ConcreteDataSourceSectionInfo *officeLocationsSectionInfo;

// Internal Array to maintain sections
// Each section consists of another Arry of objects.
@property (nonatomic, strong) NSMutableArray *sourceSections;
@property (nonatomic, strong) NSMutableArray *sectionTitles;

@end

@implementation DestinationSearchDataSource


-(instancetype)init
{
    self = [super init];
    
    if (!self)
        return nil;
    
    self.locationCells = [[NSMutableArray alloc] init];
    self.officeLocationCells = [[NSMutableArray alloc] init];
    self.sourceSections = [[NSMutableArray alloc] init];
    self.sectionTitles = [[NSMutableArray alloc] init];
    // TODO : By default load the current location
    // TODo : Add officelocation cells also , current endpoint does location search only. Expectation is that the location search will return both officelocations and regular locations.
    
    return self;
}

-(void)loadContent
{
    DestinationSearchCellData *destinationSearchCell = [[DestinationSearchCellData alloc] init];
    [destinationSearchCell setTitle:@"Office Locations"];
    [self.officeLocationCells addObject:destinationSearchCell];

    // Also add current location as a specific location
    if (self.showCurrentLocation) {
        [_officeLocationCells addObject:[self getCurrentLocationCellData]];
    }

    [self.delegate dataSourceWillChangeData:self] ;
    [self.sourceSections addObject:self.officeLocationCells];
    [self.delegate dataSource:self didChangeSection:self.officeLocationsSectionInfo atIndex:0 forChangeType:kDataSourceChangeInsert];
    
    
    [self.delegate dataSourceDidChangeContent:self];
    [self.sectionTitles insertObject:@"" atIndex:0];
}

#pragma mark - utility methods
-(void)beginEditing
{
	// MOB-19851 Remove office location section when editing
    if ([self.sourceSections containsObject:self.officeLocationCells]) {
        [self.delegate dataSourceWillChangeData:self] ;
        int index = [self.sourceSections indexOfObject:self.officeLocationCells];
        [self.sourceSections removeObject:self.officeLocationCells];
        [self.delegate dataSource:self didChangeSection:self.officeLocationsSectionInfo atIndex:index forChangeType:kDataSourceChangeDelete];
        [self.delegate dataSourceDidChangeContent:self];
        
    }
}

-(void)endEditing
{
	// MOB-19851 Add back office location section when editing
    if ([self.sourceSections containsObject:self.locationCells]) {
        [self.delegate dataSourceWillChangeData:self] ;
        [self.locationCells removeAllObjects];
        int index = [self.sourceSections indexOfObject:self.locationCells];
        [self.sourceSections removeObject:self.locationCells];
        [self.delegate dataSource:self didChangeSection:self.locationsSectionInfo atIndex:index forChangeType:kDataSourceChangeDelete];
        [self.delegate dataSourceDidChangeContent:self];
        
    }
    if(![self.sourceSections containsObject:self.officeLocationCells]){
        [self.delegate dataSourceWillChangeData:self] ;
        [self.sourceSections addObject:self.officeLocationCells];
        [self.delegate dataSource:self didChangeSection:self.officeLocationsSectionInfo atIndex:0 forChangeType:kDataSourceChangeInsert];
        [self.delegate dataSourceDidChangeContent:self];
    }
}

/*!
 This method makes the location search call and builds the locationCells Array
 */
-(void)searchLocation:(NSString *)searchString
{
    
    DLog(@"Calling Location Search for Search String : %@ ", searchString);

    // Before search is fired , clear the previous results.
    if ([self.sourceSections containsObject:self.locationCells]) {
        [self.delegate dataSourceWillChangeData:self] ;
        [self.locationCells removeAllObjects];
        int index = [self.sourceSections indexOfObject:self.locationCells];
        [self.sourceSections removeObject:self.locationCells];
        [self.delegate dataSource:self didChangeSection:self.locationsSectionInfo atIndex:index forChangeType:kDataSourceChangeDelete];
        [self.delegate dataSourceDidChangeContent:self];

    }

    // do hotel search
    CTELocationSearch *locationSearch = [[CTELocationSearch alloc] initWithAddress:searchString isAirportsOnly:NO];

    [locationSearch searchLocationsWithSuccess:^(NSArray *locations) {
        NSLog(@"Number of locations found : %d", [locations count]);
        if ([locations count] > 0) {
            // TODO : Handle the results here.
            [self handleLocationSearchResults:locations];
        }
    } failure:^(CTEError *error) {
       // TODO : Handle the error case
        ALog(@"Location Search failed with error : %@" , error.description);
    }];

    // TODO :
}

-(void)handleLocationSearchResults:(NSArray *)locations
{
    // TODO : Search retuns only locations cells for now.
    // Handle office locations also in future when endpoint is updated.
    
    for (CTELocation *location in locations) {
        // Create a LocationCell and add it into the location array
        DLog(@"Location Name: %@", location.location);
        LocationSearchCellData *locationSearchDataCell = [[LocationSearchCellData alloc] initWithCTELocation:location];
        [self.locationCells  addObject:locationSearchDataCell];
    }
//    _locationsSectionInfo =  [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.locationCells];
    
    if (![self.sourceSections containsObject:self.locationCells]) {
        //TODO : Notify UI to stop animation
        
#warning this line of code "[self.delegate dataSourceWillChangeData:self]" introduces crash sometimes (Sally)
        // Add the search results section to the view.
        [self.delegate dataSourceWillChangeData:self];
        [self.sourceSections addObject:self.locationCells];
        [self.delegate dataSource:self didChangeSection:self.locationsSectionInfo atIndex:0 forChangeType:kDataSourceChangeInsert];
        [self.delegate dataSourceDidChangeContent:self];

    }

}

-(LocationSearchCellData *)getCurrentLocationCellData
{
    CTELocation *mylocation = [[CTELocation alloc] init];
    mylocation.location = [@"Current Location" localize];
    CLLocation *currentLocation = [GlobalLocationManager sharedInstance].currentLocation;
    mylocation.longitude = currentLocation.coordinate.longitude;
    mylocation.latitude = currentLocation.coordinate.latitude;
    LocationSearchCellData *currentLocationCellData = [[LocationSearchCellData alloc] initWithCurrentLocation:mylocation];
    return currentLocationCellData;

}
#pragma mark - define properties

-(ConcreteDataSourceSectionInfo *)locationsSectionInfo
{
    _locationsSectionInfo =  [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.locationCells];
    return _locationsSectionInfo;
}

-(ConcreteDataSourceSectionInfo *)officeLocationsSectionInfo
{
    _officeLocationsSectionInfo  = [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.officeLocationCells];
    return _officeLocationsSectionInfo;
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
    
    return @[indexPath];
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


@end

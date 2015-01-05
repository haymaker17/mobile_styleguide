//
//  OfficeLocationsDataSource.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 06/08/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "OfficeLocationsDataSource.h"
#import "ConcreteDataSourceSectionInfo.h"
#import "LocationSearchCellData.h"
#import "CTELocation.h"
#import "CTELocationSearch.h"
#import "OfficeLocationResult.h"
#import "SystemConfig.h"

@interface OfficeLocationsDataSource()

@property (nonatomic, strong) NSMutableArray *officeLocationCells;
@property (nonatomic, strong) ConcreteDataSourceSectionInfo *officeLocationsSectionInfo;

// Internal Array to maintain sections
// Each section consists of another Arry of objects.
@property (nonatomic, strong) NSMutableArray *sourceSections;
@property (nonatomic, strong) NSMutableArray *sectionTitles;


@end

@implementation OfficeLocationsDataSource

-(instancetype)init
{
    self = [super init];
    if (!self){
        return nil;
    }
    self.sourceSections = [[NSMutableArray alloc] init];
    self.sectionTitles = [[NSMutableArray alloc] init];
    self.officeLocationCells = [[NSMutableArray alloc] init];
    return self;
}

-(instancetype)initWithOfficeLocationArray:(NSArray *)officeLocations
{
    self = [self init];
    if (!self)
    {
        return nil;
    }
    // load the officeLocations into the array
    self.officeLocations = officeLocations;
    return self;
}

-(void) loadContent
{
    [self searchLocation:@""];
}

/*!
 This method makes the location search call and builds the locationCells Array
 */
-(void)searchLocation:(NSString *)searchString
{
    
    DLog(@"Calling Location Search for Search String : %@ ", searchString);
    
    // Before search is fired , clear the previous results.
    if ([self.sourceSections containsObject:self.officeLocationCells]) {
        [self.delegate dataSourceWillChangeData:self] ;
        [self.officeLocationCells removeAllObjects];
        NSUInteger index = [self.sourceSections indexOfObject:self.officeLocationCells];
        [self.sourceSections removeObject:self.officeLocationCells];
        [self.delegate dataSource:self didChangeSection:self.officeLocationsSectionInfo atIndex:index forChangeType:kDataSourceChangeDelete];
        [self.delegate dataSourceDidChangeContent:self];
        
    }
    
    // do office search
	if (self.filteredOfficeLocations == nil)
	{
		NSMutableArray* newArray = [[NSMutableArray alloc] initWithCapacity:[self.officeLocations count]];
		self.filteredOfficeLocations = newArray;
	}
	
	[self.filteredOfficeLocations removeAllObjects];
	
	if (searchString == nil || [searchString length] == 0)
	{
		[self.filteredOfficeLocations addObjectsFromArray:self.officeLocations];
	}
	else
	{
		NSString *lowercaseSearchText = [searchString lowercaseString];
		
		NSUInteger officeCount = [self.officeLocations count];
		for (int i = 0; i < officeCount; i++)
		{
			OfficeLocationResult *officeLocation = (OfficeLocationResult *)self.officeLocations[i];
			NSString *lowercaseLocation = [officeLocation.location lowercaseString];
			NSRange range = [lowercaseLocation rangeOfString:lowercaseSearchText];
			if (range.location != NSNotFound)
				[self.filteredOfficeLocations addObject:self.officeLocations[i]];
		}
	}
    
    for (OfficeLocationResult *officeLocation in self.filteredOfficeLocations) {
        // Create a LocationCell and add it into the location array
        DLog(@"Location Name: %@", officeLocation.location);
        LocationSearchCellData *locationSearchDataCell = [[LocationSearchCellData alloc] initWithOfficeLocation:officeLocation];
        [self.officeLocationCells  addObject:locationSearchDataCell];
    }
    _officeLocationsSectionInfo =  [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.officeLocationCells];
    
    if (![self.sourceSections containsObject:self.officeLocationCells]) {
        //TODO : Notify UI to stop animation
        
#warning this line of code "[self.delegate dataSourceWillChangeData:self]" introduces crash sometimes (Sally)
        // Add the search results section to the view.
        [self.delegate dataSourceWillChangeData:self];
        [self.sourceSections addObject:self.officeLocationCells];
        [self.delegate dataSource:self didChangeSection:self.officeLocationsSectionInfo atIndex:0 forChangeType:kDataSourceChangeInsert];
        [self.delegate dataSourceDidChangeContent:self];
        
    }
    
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

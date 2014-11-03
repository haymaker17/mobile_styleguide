//
//  HotelRoomsListDataSource.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelRoomsListDataSource.h"
#import "ConcreteDataSourceSectionInfo.h"
#import "CTEHotelRate.h"
#import "RoomsListCellData.h"
#import "CTENetworkSettings.h"
#import "HotelDetailsCellData.h"
#import "HotelDetailsMapViewCellData.h"
#import "HotelDetailSegmentsCellData.h"
#import "PhotoAlbumTableViewCellData.h"

@interface HotelRoomsListDataSource ()
@property (nonatomic, strong) CTEHotelCellData *hotelCellData;
@property (nonatomic, strong) NSMutableArray *tableHeaderCells;
@property (nonatomic, strong) NSMutableArray *roomsListCells;
@property (nonatomic, strong) NSMutableArray *hotelDetailsCells;
@property (nonatomic, strong) NSMutableArray *hotelPhotoAlbumCells;
@property (nonatomic, strong) NSMutableArray *sourceSections;
@property (nonatomic, strong) NSMutableArray *sectionTitles;

@property(nonatomic, strong) ConcreteDataSourceSectionInfo *roomsListSectionInfo;
@property(nonatomic, strong) ConcreteDataSourceSectionInfo *hotelDetailsSectionInfo;

@end

@implementation HotelRoomsListDataSource

-(instancetype)initWithHotelCellData:(CTEHotelCellData *)hotelCellData
{
    self = [super init];
    
    if (!self)
        return nil;
    
    self.sourceSections = [[NSMutableArray alloc] init];
    self.sectionTitles = [[NSMutableArray alloc] init];
    self.tableHeaderCells = [[NSMutableArray alloc] init];
    self.roomsListCells = [[NSMutableArray alloc] init];
    self.hotelDetailsCells = [[NSMutableArray alloc] init];
    self.hotelPhotoAlbumCells = [[NSMutableArray alloc] init];
    _hotelCellData = hotelCellData;

    return self;
}

-(void)loadContent
{
    [self loadHeaderDataWithHotelCellData:self.hotelCellData];
    [self loadRoomsListData:self.hotelCellData];
}

-(void)loadHeaderDataWithHotelCellData:(CTEHotelCellData *)hotelCellData
{
    // 1. Image view Cell
    RoomsListHeaderImageCellData *imageViewCell = [[RoomsListHeaderImageCellData alloc] initWithCTEHotelCellData:hotelCellData];
    [self.tableHeaderCells addObject:imageViewCell];

    // 2. Hotel Detail cell
    [self.tableHeaderCells addObject:hotelCellData];
    
    // 3. Segment Control cell
    //
    HotelDetailSegmentsCellData *hotelDetailSegmentsCellData = [[HotelDetailSegmentsCellData alloc] init];
    [self.tableHeaderCells addObject:hotelDetailSegmentsCellData];
    
    
    [self.sourceSections insertObject:self.tableHeaderCells atIndex:0];
    [self.sectionTitles insertObject:@"" atIndex:0];
    
    _roomsListSectionInfo = [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.tableHeaderCells];
    [self.delegate dataSourceWillChangeData:self];
    [self.delegate dataSource:self didChangeSection:self.roomsListSectionInfo atIndex:0 forChangeType:kDataSourceChangeUpdate];
    [self.delegate dataSourceDidChangeContent:self];
}


-(void)loadRoomsListData:(CTEHotelCellData *)hotelCellData
{

    CTEHotel *cteHotel = [hotelCellData getCTEHotel];
    [cteHotel ratesWithCompletionBlock:^(NSArray *rates) {
        // TODO: update cteHotel with whether the rates are cached
        // show rooms
        if (self.hideWaitView) {
            self.hideWaitView();
        }
        if (rates.count > 0) {
            [self prepareRoomsListCellDataSouce:rates];
        } else {

            // TODO: update ratesWithCompletionBlock with a failure and success blocks? or something
            ALog(@"No rates returned!!");
            if (self.onRequestHotelRatesError) {
                self.onRequestHotelRatesError(@"No room rates available for this hotel.");
            }
        }
    }];

//    if ([cteHotel.ratesURL length]) {
//         DLog(@"CTENetworkSettings.serverURL: %@", [[CTENetworkSettings sharedInstance] serverURL]);
//        CTEHotelRates *cteHotelRates = [[CTEHotelRates alloc] initWithURL:cteHotel.ratesURL];
//        
//         DLog(@"Requesting hotel rates");
//        [cteHotelRates requestHotelRatesWithSuccess:^(NSArray *hotelRates){
//            if (self.hideWaitView) {
//                self.hideWaitView();
//            }
//            // show rooms
//            [self prepareRoomsListCellDataSouce:hotelRates];
//            
//        }failure:^(CTEError *error){
//            DLog(@"Failure at requesting hotel rates.");
//            if (self.hideWaitView) {
//                self.hideWaitView();
//            }
//            if (self.onRequestHotelRatesError) {
//                self.onRequestHotelRatesError([error simpleErrorMessage]);
//            }
//        }];
//    }

        // no section title required. not sure if we need this line or not
    [self.sectionTitles insertObject:@"" atIndex:1];
}

- (void)prepareRoomsListCellDataSouce:(NSArray *)rates
{
    for (CTEHotelRate *roomRate in rates) {
            RoomsListCellData *roomsListCellData = [[RoomsListCellData alloc] initWithHoteRateData:roomRate];
            [self.roomsListCells addObject:roomsListCellData];
    }

    _roomsListSectionInfo =  [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.roomsListCells];
    
    if (![self.sourceSections containsObject:self.roomsListCells]) {
        // Add the rooms list section to the view.
        [self.delegate dataSourceWillChangeData:self];
        [self.sourceSections addObject:self.roomsListCells];
        [self.delegate dataSource:self didChangeSection:self.roomsListSectionInfo atIndex:1 forChangeType:kDataSourceChangeInsert];
        [self.delegate dataSourceDidChangeContent:self];
        
    }
}

-(void)showRoomList
{
    [self.delegate dataSourceWillChangeData:self];
    // remove hotel detail cells first. when working on Photo, need to remove photo cell as well
    [self removeDataCells:self.hotelDetailsCells];
    [self removeDataCells:self.hotelPhotoAlbumCells];
    // manually change segments control selected index only because of the Find a Hotel button
    [self updateSegmentsControlCellData:1];
    
    if ([self.roomsListCells count] > 0 && ![self.sourceSections containsObject:self.roomsListCells]) {
        _roomsListSectionInfo =  [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.roomsListCells];
        [self.sourceSections addObject:self.roomsListCells];
        [self.delegate dataSource:self didChangeSection:self.roomsListSectionInfo atIndex:1 forChangeType:kDataSourceChangeInsert];
        [self.delegate dataSourceDidChangeContent:self];
    }
}

-(void)showPhotos
{
    [self.delegate dataSourceWillChangeData:self];
    // remove hotel detail cells first. when working on Photo, need to remove photo cell as well
    [self removeDataCells:self.hotelDetailsCells];
    [self removeDataCells:self.roomsListCells];
    // manually change segments control selected index only because of the Find a Hotel button
    [self updateSegmentsControlCellData:2];
    [self.delegate dataSourceDidChangeContent:self];
    [self insertHotelPhotosCell];
}

-(void)showHotelDetails
{
    [self.delegate dataSourceWillChangeData:self];
    [self removeDataCells:self.roomsListCells];
    [self removeDataCells:self.hotelPhotoAlbumCells];

    // manually change segments control selected index only because of the Find a Hotel button
    [self updateSegmentsControlCellData:0];
    [self.delegate dataSourceDidChangeContent:self];
    [self insertHotelDetailsCell];
}

-(void)removeDataCells:(NSArray *)cells
{
    if ([self.sourceSections containsObject:cells]) {
        // remove rooms
        int index = (int)[self.sourceSections indexOfObject:cells];
        [self.sourceSections removeObject:cells];
        [self.delegate dataSource:self didChangeSection:self.roomsListSectionInfo atIndex:index forChangeType:kDataSourceChangeDelete];
    }
}

-(void)insertHotelDetailsCell
{
    [self.delegate dataSourceWillChangeData:self];
    // add hotel details
    if ([self.hotelDetailsCells count] == 0) {
        HotelDetailsMapViewCellData *mapViewCellData = [[HotelDetailsMapViewCellData alloc] initWithCTEHotelCellData:self.hotelCellData];
        HotelDetailsCellData *hotelDetailsCellData = [[HotelDetailsCellData alloc] initWithCTEHotelCellData:self.hotelCellData];
        
        [self.hotelDetailsCells addObject:mapViewCellData];
        [self addHotelDetailsCallHotelCell];
        [self.hotelDetailsCells addObject:hotelDetailsCellData];
        [self addHotelDetailsFindRoomCell];
    }
    [self.sourceSections addObject:self.hotelDetailsCells];

    _hotelDetailsSectionInfo = [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.hotelDetailsCells];
    [self.delegate dataSource:self didChangeSection:self.hotelDetailsSectionInfo atIndex:1 forChangeType:kDataSourceChangeInsert];
    [self.delegate dataSourceDidChangeContent:self];
}

-(void)updateSegmentsControlCellData:(int)selectedIndex
{
    // this makes sure we're using the correct detail segment layout
    if (self.tableHeaderCells.count > 2) {
        HotelDetailSegmentsCellData *cellData = self.tableHeaderCells[2];
        cellData.selectedIndex = selectedIndex;
        
        // segmentsControl is located at index path [0 ,2]
        NSIndexPath *checkOutCellIndexPath = [NSIndexPath indexPathForRow:2 inSection:0];
        [self.delegate dataSource:self didChangeObject:cellData atIndexPath:checkOutCellIndexPath forChangeType:kDataSourceChangeUpdate newIndexPath:nil];
    }
}

#pragma mark - set up cells for hotel details view
-(void)addHotelDetailsCallHotelCell
{
    AbstractTableViewCellData *hotelDetailsCallCellData = [[AbstractTableViewCellData alloc] init];
    hotelDetailsCallCellData.cellIdentifier = @"hotelDetailsCallCell";
    hotelDetailsCallCellData.cellHeight = 50.0;
    [self.hotelDetailsCells addObject:hotelDetailsCallCellData];
}

-(void)addHotelDetailsFindRoomCell
{
    AbstractTableViewCellData *hotelDetailsFindRoom = [[AbstractTableViewCellData alloc] init];
    hotelDetailsFindRoom.cellIdentifier = @"hotelDetailsFindRoomCell";
    hotelDetailsFindRoom.cellHeight = 60.0;
    [self.hotelDetailsCells addObject:hotelDetailsFindRoom];
}

-(void)insertHotelPhotosCell
{
    [self.delegate dataSourceWillChangeData:self];
    // add hotel photo album cell
    if ([self.hotelPhotoAlbumCells count] == 0) {
        PhotoAlbumTableViewCellData *photoAlbumCellData = [[PhotoAlbumTableViewCellData alloc] initWithCTEHotelCellData:self.hotelCellData];
        [self.hotelPhotoAlbumCells addObject:photoAlbumCellData];
    }
    [self.sourceSections addObject:self.hotelPhotoAlbumCells];
    
    _hotelDetailsSectionInfo = [[ConcreteDataSourceSectionInfo alloc] initWithArray:self.hotelDetailsCells];
    [self.delegate dataSource:self didChangeSection:self.hotelDetailsSectionInfo atIndex:1 forChangeType:kDataSourceChangeInsert];
    [self.delegate dataSourceDidChangeContent:self];
    
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

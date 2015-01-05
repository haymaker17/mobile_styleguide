//
//  HotelRoomsListTableViewController.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelRoomsListTableViewController.h"
#import "AbstractTableViewCellData.h"
#import "RoomsListHeaderImageCell.h"
#import "RoomsListCellData.h"
#import "RoomsListTableViewCell.h"
#import "TravelWaitViewController.h"
#import "HotelRoomReserveViewController.h"
#import "HotelSearchMapviewViewController.h"
#import "HotelSearchTableViewCell.h"
#import "RoomListSegmentsTableViewCell.h"
#import "HotelDetailsCellData.h"
#import "HotelPhoneCellData.h"
#import "HotelDetailsTableViewCell.h"
#import "HotelDetailsMapViewCellData.h"
#import "HotelDetailsMapViewTableViewCell.h"
#import "HotelDetailsCallHotelTableViewCell.h"
#import "HotelDetailsFindRoomTableViewCell.h"
#import "HotelDetailSegmentsCellData.h"
#import "PhotoAlbumTableViewCell.h"
#import "PhotoAlbumTableViewCellData.h"
#import "ImageCollectionViewCell.h"
#import "ImagesScrollView.h"
#import "ExSystem.h"
#import "AnalyticsTracker.h"

@interface HotelRoomsListTableViewController () <ImageDownloaderOperationDelegate>

@property (nonatomic, strong) HotelRoomsListDataSource *tableData;
@property (nonatomic, strong) UIBarButtonItem *btnMapView;
@property int segmentsSelectedIndex;
@property (nonatomic, strong) NSOperationQueue *imageDownloadQueue;
@property (nonatomic, strong) UIViewController *modalVC;
@end

@implementation HotelRoomsListTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.tableData = [[HotelRoomsListDataSource alloc] initWithHotelCellData:self.hotelCellData];
    self.tableData.delegate = self;
    
    _btnMapView = [self getNavBarButtonWithImage:@"icon_nav_map" withSelector:@selector(showMapView)];
    _btnMapView.enabled = NO;
    
    // show wait view when requesting hotel rates
    [TravelWaitViewController showWithText:[NSString stringWithFormat:@"Please wait while showing hotel rooms"] animated:YES];
    
    __weak HotelRoomsListTableViewController *weakSelf = self;
    [self.tableData setHideWaitView:^{
    	weakSelf.btnMapView.enabled = YES;
        [TravelWaitViewController hideAnimated:YES withCompletionBlock:nil];
    }];
    
    // show alert view when there's no hotel rates available
    [self.tableData setOnRequestHotelRatesError:^(NSString *error) {
        UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:error delegate:weakSelf cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];
        [alert show];
    }];
    
    [self.tableData loadContent];

    [self.navigationItem setRightBarButtonItem:self.btnMapView];
    [self.navigationController setToolbarHidden:YES];
    // Register common cell
    [self.tableView registerClass:[HotelSearchTableViewCell class] forCellReuseIdentifier:@"HotelSearchTableViewCell"];
    
    [AnalyticsTracker initializeScreenName:@"Hotel OverView"];

}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [TravelWaitViewController hideAnimated:YES withCompletionBlock:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Generic class methods
/*!
 Creates a Navbar buttons with given image and selector.
 */
-(UIBarButtonItem *)getNavBarButtonWithImage:(NSString *)imgName withSelector:(SEL)selectorName
{
    UIButton* mbtn =[UIButton buttonWithType:UIButtonTypeCustom];
    UIImage* mImage = [UIImage imageNamed:imgName];
    [mbtn setImage:mImage forState:UIControlStateNormal];
    [mbtn addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    mbtn.frame = CGRectMake(0, 0, mImage.size.width, mImage.size.height);
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc]initWithCustomView:mbtn];
    
    return menuButton;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.tableData numberOfSections];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.tableData.sections[section] count];
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    AbstractTableViewCellData *cellData = [self.tableData itemAtIndexPath:indexPath];
    
    if ([cellData isKindOfClass:[PhotoAlbumTableViewCellData class]]) {
        PhotoAlbumTableViewCellData *photoAlbumCellData = (PhotoAlbumTableViewCellData *)cellData;
        if (!photoAlbumCellData.isCellHeightSetAccordingToContentSize) { // Calculate the height manually based on number of photos to be displayed, 2 photocells per row with spacing set at 8
            CGSize individualPhotoCellSize = [self sizeForCollectionViewCell];
            NSUInteger numberOfImagesToDisplay = self.hotelCellData.downLoadableUIImages.count ? self.hotelCellData.downLoadableUIImages.count : self.hotelCellData.cteHotel.images.count;
            int numberOfRowsOfHotelCells = (int) (numberOfImagesToDisplay / 2.0 + 0.5); // 2 photocells per row
            CGFloat heightOfPhotoAlbumView = (individualPhotoCellSize.height + 8) * numberOfRowsOfHotelCells + 8; // spacing between photocells = 8
            
            return heightOfPhotoAlbumView;
        }
        
    }
    
    return cellData.cellHeight;
}

//- (UIView*)tableView:(UITableView *)tableView viewfor
//{
////    AbstractTableViewCellData *dataObj = self.tableData.sections[section][section.row];
//    UIView *sectionHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, self.tableView.frame.size.width, 13.0f)];
//    [sectionHeaderView setBackgroundColor:[UIColor colorWithRed:233.0/255.0 green:233.0/255.0 blue:233.0/255.0 alpha:1.0]];
//    return sectionHeaderView;
//}


 - (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
 {
     AbstractTableViewCellData *dataObject = [self.tableData itemAtIndexPath:indexPath];
     
     if ([dataObject.cellIdentifier isEqualToString:@"RoomsListHeaderImageCell"]) {     // header image cell
         RoomsListHeaderImageCellData *roomsListCellData = (RoomsListHeaderImageCellData *)dataObject;
         RoomsListHeaderImageCell *cell = [self.tableView dequeueReusableCellWithIdentifier:dataObject.cellIdentifier forIndexPath:indexPath];
         // TODO : Do the actual work here
         [cell setCellData:roomsListCellData];
         return cell;
     }
     if ([dataObject.cellIdentifier isEqualToString:@"hotelDetailSegmentsCell"]) {     // Room details segment cell
         HotelDetailSegmentsCellData *cellData =  (HotelDetailSegmentsCellData *)dataObject;

         RoomListSegmentsTableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:dataObject.cellIdentifier forIndexPath:indexPath];
         cell.segmentedCtrl.selectedSegmentIndex = cellData.selectedIndex;
         [cell setOnSegmentsSelected:^(NSDictionary *info){
             [self segmentDidSelected:info];
         }];
         return cell;
     }

     
     if ([dataObject.cellIdentifier isEqualToString:@"HotelSearchTableViewCell"]) {
         
         CTEHotelCellData *cellData =  (CTEHotelCellData *)dataObject;
         HotelSearchTableViewCell *hotelListCell = [self.tableView dequeueReusableCellWithIdentifier:dataObject.cellIdentifier];
         
         if (hotelListCell.hotelName == nil)
         {
             NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelSearchTableViewCell" owner:self options:nil];
             for (id oneObject in nib)
                 if ([oneObject isKindOfClass:[HotelSearchTableViewCell class]])
                     hotelListCell = (HotelSearchTableViewCell *)oneObject;
         }
         
         [hotelListCell setCellData:cellData indexPath:indexPath];
         [hotelListCell.hotelPrice setHidden:YES];
         
         [hotelListCell displayCellAsEnabled:YES];
         [hotelListCell setSelectionStyle:UITableViewCellSelectionStyleNone];
         if ([ExSystem is7Plus]) {
            hotelListCell.separatorInset = UIEdgeInsetsMake(0, 1000, 0, 0);
         }
         return hotelListCell;
     }
 
     if ([dataObject isKindOfClass:[RoomsListCellData class]]){
         RoomsListCellData *roomsListCellData = (RoomsListCellData *)dataObject;
         RoomsListTableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"roomsListTableViewCell" forIndexPath:indexPath];
         [cell setCellData:roomsListCellData];

         return cell;
     }
     
     if ([dataObject isKindOfClass:[HotelDetailsMapViewCellData class]]){
         HotelDetailsMapViewCellData *cellData = (HotelDetailsMapViewCellData *)dataObject;
         HotelDetailsMapViewTableViewCell *mapTableViewCell = [self.tableView dequeueReusableCellWithIdentifier:@"hotelDetailsMapViewCell" forIndexPath:indexPath];
         [mapTableViewCell setCellData:cellData];
         
         return mapTableViewCell;
     }
     
     if ([dataObject isKindOfClass:[HotelDetailsCellData class]]){
         HotelDetailsCellData *cellData = (HotelDetailsCellData *)dataObject;
         HotelDetailsTableViewCell *hotelDetailsCell = [self.tableView dequeueReusableCellWithIdentifier:@"hotelDetailsAddressCell" forIndexPath:indexPath];
         [hotelDetailsCell setCellData:cellData];
         return hotelDetailsCell;
     }
     
     if ([dataObject isKindOfClass:[HotelPhoneCellData class]]) {
         HotelDetailsCallHotelTableViewCell *hotelCallCell = [self.tableView dequeueReusableCellWithIdentifier:@"hotelDetailsCallCell" forIndexPath:indexPath];
         HotelPhoneCellData *cellData = (HotelPhoneCellData *)dataObject;
         [hotelCallCell setCellData:cellData];
         return hotelCallCell;
     }
     
     if ([dataObject.cellIdentifier isEqualToString:@"hotelDetailsFindRoomCell"]) {
         HotelDetailsFindRoomTableViewCell *tableViewCell = [self.tableView dequeueReusableCellWithIdentifier:@"hotelDetailsFindRoomCell" forIndexPath:indexPath];
         HotelDetailsCellData *cellData = (HotelDetailsCellData *)dataObject;
         [tableViewCell setBtnFindRoomPressed:^{
             [self.tableData showRoomList];
         }];
         return tableViewCell;
     }
     
     if ([dataObject isKindOfClass:[PhotoAlbumTableViewCellData class]]) {
         PhotoAlbumTableViewCell *photoAlbumCell = [self.tableView dequeueReusableCellWithIdentifier:dataObject.cellIdentifier forIndexPath:indexPath];
         [photoAlbumCell setCollectionViewDataSourceDelegate:self];
         PhotoAlbumTableViewCellData *cellData = (PhotoAlbumTableViewCellData *)dataObject;
         if (!cellData.isCellHeightSetAccordingToContentSize) {
             cellData.cellHeight = photoAlbumCell.photosCollectionView.superview.frame.size.height;
             cellData.isCellHeightSetAccordingToContentSize = YES;
             [self.tableView beginUpdates];
             [self.tableView endUpdates]; // This resizes the row (doesn't work in iOS 7 though, hence view height is manually calculated in heightForRowAtIndexPath:)
         }
         return photoAlbumCell;
     }
     
     return nil;
 }

// In a xib-based application, navigation from a table can be handled in -tableView:didSelectRowAtIndexPath:
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    AbstractTableViewCellData *dataObj = self.tableData.sections[indexPath.section][indexPath.row];
    
    if ([dataObj isKindOfClass:[HotelDetailsMapViewCellData class]]) {
        [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"MapView:Details" eventLabel:nil eventValue:nil];
        [self showMapView];
    }
    else if ([dataObj.cellIdentifier isEqualToString:@"hotelDetailsFindRoomCell"]){
        // update selected index
        [self.tableData updateSegmentsControlCellData:1];
    }
    else if( [dataObj isKindOfClass:[RoomsListCellData class]]) {
        RoomsListCellData *data = [self.tableData itemAtIndexPath:indexPath];
        CTEHotelRate *selectedRate = [data getHotelRatesData];
        
        HotelRoomReserveViewController *viewController = [[HotelRoomReserveViewController alloc] initWithSelectedRate:selectedRate];
        [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Room Select" eventLabel: nil eventValue:nil];
        [self.navigationController pushViewController:viewController animated:YES];
    }
    
    [self.tableView deselectRowAtIndexPath:indexPath animated:NO];
}

// For Hotel Images
#pragma mark - UICollectionView data source

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.hotelCellData.downLoadableUIImages.count ? self.hotelCellData.downLoadableUIImages.count : self.hotelCellData.cteHotel.images.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    ImageCollectionViewCell *photoCell = [collectionView dequeueReusableCellWithReuseIdentifier:@"ImageCollectionViewCell" forIndexPath:indexPath];
    DownloadableUIImage * downloadableImage = (DownloadableUIImage *)self.hotelCellData.downLoadableUIImages[indexPath.row];
    
    if (downloadableImage.hasImage) {
        photoCell.imageView.image = downloadableImage.image;
    }
    else if (!downloadableImage.failed){
        // Download the image for display
        [self.hotelCellData downloadHotelImages:self.imageDownloadQueue indexPath:indexPath downloadableUIImage:downloadableImage delegate:self];
    }

    return photoCell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return [self sizeForCollectionViewCell];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    NSMutableArray *imagesArray = [[NSMutableArray alloc] init];
    for (int i = 0; i < self.hotelCellData.downLoadableUIImages.count; i++) {
        DownloadableUIImage *downloadableImage = self.hotelCellData.downLoadableUIImages[i];
        if (downloadableImage.hasImage) {
            [imagesArray addObject:downloadableImage.image];
        }
    }
    
    self.modalVC = [[UIViewController alloc] init];
    self.modalVC.view.backgroundColor=[UIColor blackColor];
    self.modalVC.view.userInteractionEnabled=YES;
    
    ImagesScrollView *imageScrollView = [[ImagesScrollView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    imageScrollView.hotelCellData = self.hotelCellData;
    imageScrollView.selectedIndex = indexPath.row;
    imageScrollView.arrayOfDownloadableImages = self.hotelCellData.downLoadableUIImages;//[imagesArray copy];
    [imageScrollView.closeButton addTarget:self action:@selector(closeFullScreenPhotosVC:) forControlEvents:UIControlEventTouchUpInside];
    [self.modalVC.view addSubview:imageScrollView];
    
    [self presentViewController:self.modalVC animated:NO completion:nil];
}

- (void)closeFullScreenPhotosVC:(id)sender
{
    [self.modalVC dismissViewControllerAnimated:NO completion:nil];
    self.modalVC = nil;
}

- (CGSize)sizeForCollectionViewCell
{
    CGFloat width = self.tableView.bounds.size.width;
    CGFloat cellWidth = (width - 24)/2;
    CGFloat cellHeight = (cellWidth * 4)/6;
    return CGSizeMake(cellWidth, cellHeight);
}

#pragma mark - ImageDownloaderOperationDelegate
-(void)ImageDownloaderOperationDidFinish:(ImageDownloaderOperation *)downloader
{
    // get the big image to display
    if (downloader.downloadableImage.image != nil)
    {
        UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:1]];
        if ([cell isKindOfClass:[PhotoAlbumTableViewCell class]]) {
            PhotoAlbumTableViewCell *photoAlbumCell = (PhotoAlbumTableViewCell *)cell;
            [photoAlbumCell.photosCollectionView reloadItemsAtIndexPaths:@[downloader.indexPathInTableView]];
        }
    }
}

- (NSOperationQueue *)imageDownloadQueue {
    if (!_imageDownloadQueue) {
        _imageDownloadQueue = [[NSOperationQueue alloc] init];
        _imageDownloadQueue.name = @"Download Queue";
        // Let the OS manage
        _imageDownloadQueue.maxConcurrentOperationCount = 5;
    }
    return _imageDownloadQueue;
}



#pragma mark - navigation

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if([segue.identifier isEqualToString:@"showSingleHotelOnMap"])
    {
        // Set the mapviewidentifier
        UINavigationController *nav = segue.destinationViewController;
        
        HotelSearchMapviewViewController *destination = (HotelSearchMapviewViewController *)nav.topViewController;
        destination.hotelList = @[self.hotelCellData];
        destination.isSingleMapView = YES;

    }
}

-(void)showMapView
{
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Map:Button on Top" eventLabel:nil eventValue:nil];
    [self performSegueWithIdentifier:@"showSingleHotelOnMap" sender:self];
}

#pragma mark - segment selected
-(void)segmentDidSelected:(NSDictionary *)info
{
    int index = [[info objectForKey:@"selectedSegmentIndex"] intValue];
    _segmentsSelectedIndex = index;
    
    // return String for Google analyze
    NSString *selectedSegment = @"";
    
    if (index == 0) {
        [self.tableData showHotelDetails];
        selectedSegment = @"Details";
    }
    else if (index == 1){
        [self.tableData showRoomList];
        selectedSegment = @"Rooms";
    }
    else{
        [self.tableData showPhotos];
        selectedSegment = @"Photos";
    }
    
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:selectedSegment eventLabel:nil eventValue:nil];
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
    
    // adding this line just for refresh the header cell after checking if there's any right image available
    // Commented as it was causing PhotoAlbum cell to be overlapped on Rooms cells in iOS 7 // MOB-21522
    //[self.tableView reloadData];
}

#pragma mark - alert view delegate methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    if (buttonIndex == 0) {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

#pragma mark - memory management

- (void)viewDidUnload
{
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
	self.tableData = nil;
    [super viewDidUnload];
}

- (void)dealloc
{
	self.tableData.delegate = nil;
    [TravelWaitViewController hideAnimated:YES withCompletionBlock:nil];
    [self.imageDownloadQueue cancelAllOperations];
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end

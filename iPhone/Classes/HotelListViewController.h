//
//  HotelListViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/23/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HotelCollectionViewController.h"
#import "EntityHotelBooking.h"
#import "EntityHotelCheapRoom.h"
#import "HotelBookingManager.h"
#import "ExMsgRespondDelegate.h"
#import "FormatUtils.h"
#import "HotelSearchResultsViewController.h"

@interface HotelListViewController : HotelCollectionViewController <UIActionSheetDelegate, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate, ExMsgRespondDelegate>
{
}

//@property (nonatomic, retain) NSString *hotelSearchCriteria;
@property (nonatomic, strong) HotelSearchResultsViewController *hotelSearchMVC;
@property int startPos;
@property int numRecords;
@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;

@property (nonatomic, strong) IBOutlet UITableView	*tblView;
@property (nonatomic, strong) NSMutableArray		*sortedHotels;
@property (nonatomic) NSUInteger					sortOrder;

@property (nonatomic, strong) IBOutlet UILabel         *lblHeader;
@property (nonatomic, strong) IBOutlet UILabel         *lblSubheading;
@property (strong, nonatomic) IBOutlet UIView *viewForTableViewHeader;
@property (strong, nonatomic) IBOutlet UILabel *lblBenchmark;
@property (nonatomic, strong) NSIndexPath              *lastIndexPath;
- (IBAction)priceToBeatHeaderInfoClicked:(UIButton *)sender;

- (void) setSeedData;
- (void) sortHotels;
- (void) populateSortedHotels;

#pragma mark - Heading
-(void) updateHeading;
-(void) configureCell:(HotelListCell*)cell indexPath:(NSIndexPath *)indexPath;

-(void) refetchData;
-(IBAction) fetchMore:(id)sender;
@end

//
//  RoomListViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"
#import "HotelResult.h"
#import "HotelSummaryDelegate.h"
#import "EntityHotelBooking.h"
#import "RoomListCell.h"
#import "EntityHotelRoom.h"
#import "EntityHotelViolation.h"
#import "ImageViewerMulti.h"

@class RoomListSummaryCell;
@class RoomListCell;
@class HotelSearch;
@class HotelSearchCriteria;

@interface RoomListViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource, HotelSummaryDelegate, NSFetchedResultsControllerDelegate>
{
	HotelSearch				*hotelSearch;
    EntityHotelBooking      *hotelBooking;
    
    UILabel					*hotelName;
	UILabel					*address1;
	UILabel					*address2;
	UILabel					*address3;
	UILabel					*phone;
	UILabel					*distance;
	UILabel					*starRating;
	UILabel					*shadowStarRating;
	UILabel					*notRated;
	BOOL					isAddressLinked;
	int						currentPage;
	
	UIImageView				*ivHotel;
	UIButton				*btnHotel;
	NSMutableArray			*aImageURLs;
    ImageViewerMulti		*imageViewerMulti;
    UIImageView             *ivStars, *ivDiamonds;
    UILabel                 *lblPreferred;
    UIView                  *viewHeader;
	UITableView		*tblView;
}

@property (nonatomic, strong) IBOutlet UITableView	*tblView;

@property (nonatomic, strong) IBOutlet UILabel                 *lblPreferred;
@property (nonatomic, strong) IBOutlet UILabel				*hotelName;
@property (nonatomic, strong) IBOutlet UILabel				*address1;
@property (nonatomic, strong) IBOutlet UILabel				*address2;
@property (nonatomic, strong) IBOutlet UILabel				*address3;
@property (nonatomic, strong) IBOutlet UILabel				*phone;
@property (nonatomic, strong) IBOutlet UILabel				*distance;
@property (nonatomic, strong) IBOutlet UILabel				*starRating;
@property (nonatomic, strong) IBOutlet UILabel				*shadowStarRating;
@property (nonatomic, strong) IBOutlet UILabel				*notRated;
@property (nonatomic) BOOL									isAddressLinked;
@property (nonatomic) int									currentPage;
@property (nonatomic, strong) IBOutlet UIImageView			*ivHotel;
@property (nonatomic, strong) IBOutlet UIButton				*btnHotel;
@property (strong, nonatomic) IBOutlet UIView *viewForTableViewHeader;
@property (strong, nonatomic) IBOutlet UILabel *lblBenchmark;
@property (nonatomic, strong) NSMutableArray				*aImageURLs;
@property (nonatomic, strong) IBOutlet UIImageView             *ivStars;
@property (nonatomic, strong) IBOutlet UIImageView             *ivDiamonds;
@property (nonatomic, strong) IBOutlet UIView               *viewHeader;
@property (nonatomic, strong) ImageViewerMulti				*imageViewerMulti;
@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;
- (IBAction)priceToBeatHeaderInfoClicked:(UIButton *)sender;

@property (nonatomic, strong) NSString      *travelPointsInBank;
@property (nonatomic, strong) HotelSearch	*hotelSearch;
@property (nonatomic, strong) EntityHotelBooking *hotelBooking;
@property (nonatomic, strong) NSArray *roomList;

@property (strong, nonatomic) NSMutableArray                *taFields;  // TravelAuth fields for GOV
@property BOOL isVoiceBooking;

- (void)initData:(NSMutableDictionary*)paramBag;

- (void)makeToolbar:(HotelSearchCriteria*)hotelSearchCriteria roomCount:(NSNumber*)roomCount;

- (UIBarButtonItem*)makeRoomCountButton:(int)roomCount;

- (RoomListCell*)makeRoomListCell;

- (void)reserveRoomAtIndex:(int)roomIndex;

-(void)configureCell:(RoomListCell*)cell indexPath:(NSIndexPath *)indexPath;

-(void) refetchData;

-(void)configureHeader:(BOOL)showAddressLink;

-(IBAction) showHotelImages:(id)sender;
-(IBAction)showDetails:(id)sender;
@end

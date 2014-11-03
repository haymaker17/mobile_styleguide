//
//  RoomListSummaryCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIImageScrollView.h"
#import "ImageViewerMulti.h"
#import "EntityHotelBooking.h"
#import "HotelSummaryDelegate.h"

@class RoomListViewController;
@class CachedImageView;
@class HotelResult;
@class MobileViewController;

@interface RoomListSummaryCell : UITableViewCell
{
	id<HotelSummaryDelegate>	__weak hotelSummaryDelegate;
	UIImageScrollView		*scroller;
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
}

extern NSString * const ROOM_LIST_SUMMARY_CELL_REUSABLE_IDENTIFIER;
@property (nonatomic, strong) IBOutlet UILabel                 *lblPreferred;
@property (nonatomic, weak) id<HotelSummaryDelegate>		hotelSummaryDelegate;
@property (nonatomic, strong) IBOutlet UIImageScrollView	*scroller;
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
@property (nonatomic, strong) NSMutableArray				*aImageURLs;
@property (nonatomic, strong) ImageViewerMulti				*imageViewerMulti;

@property (nonatomic, strong) IBOutlet UIImageView             *ivStars;
@property (nonatomic, strong) IBOutlet UIImageView             *ivDiamonds;

+(RoomListSummaryCell*) makeCellForTableView:(UITableView*)tableView owner:(id)owner;
+(RoomListSummaryCell*) makeAndConfigureCellForTableView:(UITableView*)tableView owner:(MobileViewController*)owner hotel:(EntityHotelBooking*)hotelResult showAddressLink:(BOOL)showAddressLink;

-(void)configure:(id)owner hotel:(EntityHotelBooking *)hotelResult showAddressLink:(BOOL)showAddressLink;

-(NSString*)reuseIdentifier;

-(IBAction)btnAddress:(id)sender;
-(IBAction)btnPhone:(id)sender;


//-(void)configureWithImagePairs:(NSArray *)propertyImagePairs Owner:(MobileViewController*)owner;
//-(void) fillImagesFromURLs: (NSMutableArray*) imageURLs Owner:(MobileViewController*)owner;
-(IBAction) showHotelImages:(id)sender;
//-(NSMutableArray*)getImageURLs:(NSArray *)propertyImagePairs;
@end



//
//  BookingImageCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ImageViewerMulti.h"
#import "MobileViewController.h"

@class UIImageScrollView;

@interface BookingImageCell : UITableViewCell
{
	UIImageScrollView	*scroller;
	UILabel				*descriptionLabel;
	
	//image viewer stuff//////////////////////
	ImageViewerMulti		*imageViewerMulti;
	NSMutableArray			*aImageURLs;
	MobileViewController	*parentVC;
	UIButton				*btnImage;
	UIImageView				*ivImage;
	//////////////////////////////////////////
}

@property (nonatomic, strong) IBOutlet UIImageScrollView	*scroller;
@property (nonatomic, strong) IBOutlet UILabel				*descriptionLabel;

//image viewer stuff///////////////////////////////////////////////////////////////
@property (nonatomic, strong) ImageViewerMulti				*imageViewerMulti;
@property (nonatomic, strong) NSMutableArray				*aImageURLs;
@property (nonatomic, strong) MobileViewController			*parentVC;
@property (nonatomic, strong) IBOutlet UIButton				*btnImage;
@property (nonatomic, strong) IBOutlet UIImageView			*ivImage;
-(void)configureImages:(id)owner propertyImagePairs:(NSArray*)propertyImagePairs;
-(IBAction) showHotelImages:(id)sender;
///////////////////////////////////////////////////////////////////////////////////

+(BookingImageCell*)makeCell:(UITableView*)tableView owner:(id)owner description:(NSString*)description propertyImagePairs:(NSArray*)propertyImagePairs;

@end

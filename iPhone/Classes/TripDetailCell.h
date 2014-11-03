//
//  TripDetailCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
@class TripsCellBig;
@class TripDetailCellBig;

#import "SegmentData.h"
#import "Detail.h"
#import "DetailViewController.h"

@interface TripDetailCell : UITableViewCell {
	UILabel			*labelHead;
	UILabel			*labelActionTime;
	UILabel			*labelVendor;
	UILabel			*labelFromToLocation;
	UILabel			*labelDetails;
	UILabel			*labelVendorDetail;
	UIImageView		*imgHead;
	UIButton		*btnDrill;
	TripDetailCellBig	*rootCell;

	NSUInteger		currentRow;
	UIButton		*btnAction;
	UIImageView		*imgVendor;
	NSString		*idKey;
	NSString		*tripKey, *segmentType, *bookingKey;
	SegmentData		*segment;
	UITextView		*txtView;
	
	NSMutableArray	*aDetails;
	Detail			*detail;
	DetailViewController *dVC;
	
	UILabel			*lblDanger;
}

@property (nonatomic, strong) IBOutlet UILabel *labelHead;
@property (nonatomic, strong) IBOutlet UILabel *labelActionTime;
@property (nonatomic, strong) IBOutlet UILabel *labelVendor;
@property (nonatomic, strong) IBOutlet UILabel *labelFromToLocation;
@property (nonatomic, strong) IBOutlet UILabel *labelDetails;
@property (nonatomic, strong) IBOutlet UILabel *labelVendorDetail;
@property (nonatomic, strong) IBOutlet UIImageView *imgHead;
@property (nonatomic, strong) IBOutlet UIButton *btnDrill;
@property (nonatomic, strong) IBOutlet UIButton *btnAction;
@property (nonatomic, strong) TripDetailCellBig *rootCell;
//@property (nonatomic, strong) RootViewController *rootVC;
@property (nonatomic) NSUInteger currentRow;
@property (nonatomic, strong) IBOutlet UIImageView *imgVendor;
@property (nonatomic, strong) NSString *idKey;
@property (nonatomic, strong) NSString *tripKey;
@property (nonatomic, strong) NSString *segmentType;
@property (nonatomic, strong) NSString *bookingKey;
@property (nonatomic, strong) SegmentData *segment;
@property (nonatomic, strong) IBOutlet UITextView		*txtView;
@property (nonatomic, strong) IBOutlet UILabel			*lblDanger;

@property (nonatomic, strong) Detail			*detail;
@property (nonatomic, strong) DetailViewController *dVC;
@property (nonatomic, strong) NSMutableArray	*aDetails;

- (IBAction)buttonDrillPressed:(id)sender;

-(IBAction)goSomeplace:(id)sender;
-(void)callNumber:(NSString *)phoneNum;
//-(IBAction)loadWebView:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle;
@end

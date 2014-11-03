//
//  ItinDetailsCellInfo.h
//  ConcurMobile
//
//  Created by Paul Kramer on 2/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
@class RootViewController;
@class ItinDetailsViewController;

@interface ItinDetailsCellInfo : UITableViewCell 
{
	UILabel			*labelPhone, *labelMap, *labelAddress1, *labelAddress2;
	UIButton		*btnPhone, *btnMap;
	UIImageView		*imgVendor;
	NSString		*mapAddress, *vendorName, *vendorCode, *phoneNumber;
	RootViewController	*rootVC;
	ItinDetailsViewController *idVC;

}

extern NSString * const ITIN_DETAILS_CELL_INFO_REUSE_IDENTIFIER;

@property (strong, nonatomic) IBOutlet UILabel *labelPhone;
@property (strong, nonatomic) IBOutlet UILabel *labelMap;
@property (strong, nonatomic) IBOutlet UILabel *labelAddress1;
@property (strong, nonatomic) IBOutlet UILabel *labelAddress2;
@property (strong, nonatomic) IBOutlet UIButton	*btnPhone;
@property (strong, nonatomic) IBOutlet UIButton *btnMap;
@property (strong, nonatomic) IBOutlet UIImageView *imgVendor;
@property (strong, nonatomic) NSString *mapAddress;
@property (strong, nonatomic) RootViewController *rootVC;
@property (strong, nonatomic) ItinDetailsViewController *idVC;
@property (strong, nonatomic) NSString *vendorName;
@property (strong, nonatomic) NSString *vendorCode;
@property (strong, nonatomic) NSString *phoneNumber;

-(IBAction)dialNumber;
-(IBAction)goSomeplace;

@end

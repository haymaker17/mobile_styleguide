//
//  ItinDetailsCellLabelPad.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
@class RootViewController;
#import "ItinDetailsViewController.h"


@interface ItinDetailsCellLabelPad : UITableViewCell 
{
	UILabel						*labelLabel, *labelValue, *labelVendor,*labelValue1, *labelValue2,*labelValue3, *labelValue4,*labelValue5, *labelValue6;
	UILabel						*lv1, *lv2,*lv3, *lv4,*lv5, *lv6;
    UILabel                     *lblWhiteBack, *lblShadow;
	UIImageView					*imgView, *ivIcon, *ivBackground;
	UIButton					*btn1, *btn2, *btnCancel;
	NSString					*specialValueWeb, *specialValuePhone, *webViewTitle;
	RootViewController			*rootVC;
	ItinDetailsViewController	*idVC;
}

extern NSString * const ITIN_DETAILS_CELL_LABEL_REUSE_IDENTIFIER;

@property (nonatomic, strong) NSString *segmentType;
@property (nonatomic, strong) IBOutlet UILabel *lblWhiteBack;
@property (nonatomic, strong) IBOutlet UILabel *lblShadow;
@property (nonatomic, strong) IBOutlet UILabel *labelLabel;
@property (nonatomic, strong) IBOutlet UILabel *labelValue;
@property (nonatomic, strong) IBOutlet UILabel *labelVendor;
@property (nonatomic, strong) IBOutlet UILabel *labelValue1;
@property (nonatomic, strong) IBOutlet UILabel *labelValue2;
@property (nonatomic, strong) IBOutlet UILabel *labelValue3;
@property (nonatomic, strong) IBOutlet UILabel *labelValue4;
@property (nonatomic, strong) IBOutlet UILabel *labelValue5;
@property (nonatomic, strong) IBOutlet UILabel *labelValue6;
@property (nonatomic, strong) IBOutlet UIImageView *imgView;
@property (nonatomic, strong) IBOutlet UIImageView *ivIcon;
@property (nonatomic, strong) IBOutlet UIImageView *ivBackground;
@property (nonatomic, strong) IBOutlet UIButton	*btn1;
@property (nonatomic, strong) IBOutlet UIButton	*btn2;
@property (nonatomic, strong) NSString	*specialValueWeb; 
@property (nonatomic, strong) NSString	*specialValuePhone;
@property (nonatomic, strong) NSString	*webViewTitle;

@property (nonatomic, strong) IBOutlet UILabel	*lv1;
@property (nonatomic, strong) IBOutlet UILabel	*lv2;
@property (nonatomic, strong) IBOutlet UILabel	*lv3;
@property (nonatomic, strong) IBOutlet UILabel	*lv4;
@property (nonatomic, strong) IBOutlet UILabel	*lv5;
@property (nonatomic, strong) IBOutlet UILabel	*lv6;

@property (strong, nonatomic) RootViewController *rootVC;
@property (strong, nonatomic) ItinDetailsViewController *idVC;

@property (strong, nonatomic) IBOutlet UIButton *btnCancel;
-(IBAction)btnPressed:(id)sender;
-(IBAction)btnCancelPressed:(id)sender;
-(void)resetCellConfiguration;

+(ItinDetailsCellLabelPad*)makeCell:(UITableView*)tableView cellLabel:(NSString*)label cellValue:(NSString*)val;
+(ItinDetailsCellLabelPad*)makeVendorCell:(UITableView*)tableView vendor:(NSString*)vendorName;
+(ItinDetailsCellLabelPad*)makeLocationCell:(UITableView*)tableView location:(NSString*)location;
+(ItinDetailsCellLabelPad*)makePhoneCell:(UITableView*)tableView phoneNumber:(NSString*)phoneNumber;
+(ItinDetailsCellLabelPad*)makeIconValueCell:(UITableView*)tableView iconName:(NSString*)iconName value:(NSString*)value;

@end

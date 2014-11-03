//
//  ItinDetailsCellLabel.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ItinDetailsViewController.h"


@interface ItinDetailsCellLabel : UITableViewCell 
{
	UILabel						*labelLabel, *labelValue, *labelVendor,*labelValue1, *labelValue2;
	UIImageView					*imgView, *ivIcon;
	UIButton					*btn1, *btn2;
	NSString					*specialValueWeb, *specialValuePhone, *webViewTitle;
}

extern NSString * const ITIN_DETAILS_CELL_LABEL_REUSE_IDENTIFIER;

@property (nonatomic, strong) IBOutlet UILabel *labelLabel;
@property (nonatomic, strong) IBOutlet UILabel *labelValue;
@property (nonatomic, strong) IBOutlet UILabel *labelVendor;
@property (nonatomic, strong) IBOutlet UILabel *labelValue1;
@property (nonatomic, strong) IBOutlet UILabel *labelValue2;
@property (nonatomic, strong) IBOutlet UIImageView *imgView;
@property (nonatomic, strong) IBOutlet UIImageView *ivIcon;
@property (nonatomic, strong) IBOutlet UIButton	*btn1;
@property (nonatomic, strong) IBOutlet UIButton	*btn2;
@property (nonatomic, strong) NSString	*specialValueWeb; 
@property (nonatomic, strong) NSString	*specialValuePhone;
@property (nonatomic, strong) NSString	*webViewTitle;

@property (strong, nonatomic) UIViewController *idVC;

//@property (strong, nonatomic) ItinDetailsViewController *idVC;

-(IBAction)btnPressed:(id)sender;

-(void)resetCellConfiguration;

+(ItinDetailsCellLabel*)makeCell:(UITableView*)tableView cellLabel:(NSString*)label cellValue:(NSString*)val;
+(ItinDetailsCellLabel*)makeVendorCell:(UITableView*)tableView vendor:(NSString*)vendorName;
+(ItinDetailsCellLabel*)makeLocationCell:(UITableView*)tableView location:(NSString*)location;
+(ItinDetailsCellLabel*)makePhoneCell:(UITableView*)tableView phoneNumber:(NSString*)phoneNumber;
+(ItinDetailsCellLabel*)makeIconValueCell:(UITableView*)tableView iconName:(NSString*)iconName value:(NSString*)value;

@end

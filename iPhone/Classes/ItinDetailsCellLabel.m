//
//  ItinDetailsCellLabel.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ItinDetailsCellLabel.h"
#import "WebViewController.h"


@implementation ItinDetailsCellLabel

@synthesize labelLabel;
@synthesize labelValue;
@synthesize imgView;
@synthesize labelVendor;
@synthesize labelValue1;
@synthesize labelValue2;
@synthesize specialValueWeb;
@synthesize specialValuePhone;
@synthesize btn1;
@synthesize btn2;

@synthesize webViewTitle, ivIcon;

NSString * const ITIN_DETAILS_CELL_LABEL_REUSE_IDENTIFIER = @"ItinDetailsCellLabel"; // TODO: Make consistent with reusable identifier in Interface Builder!

-(IBAction)btnPressed:(id)sender
{
	UIButton *btn = (UIButton *)sender;
	if (btn.tag == 1) 
	{
		//do telephone
		[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", specialValuePhone]]];
	}
	else if (btn.tag == 2) 
	{
		//do web view
		//[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"http://%@", specialValueWeb]]];
		WebViewController *webView = [[WebViewController alloc] init];
		//webView.rootViewController = rootVC;
		webView.url = [NSString stringWithFormat:@"http://%@", specialValueWeb];
		webView.viewTitle = webViewTitle;
		[self.idVC presentViewController:webView animated:YES completion:nil];
		
	}
}


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}




#pragma mark -
#pragma mark Make Cell Methods

-(void)resetCellConfiguration
{
	[btn1 setHidden:YES];
	[labelLabel setHidden:YES];
	[labelValue setHidden:YES];
	[labelVendor setHidden:YES];
	[ivIcon setHidden:YES];
	labelValue.textColor = [UIColor blackColor];
	self.accessoryType = UITableViewCellAccessoryNone;
}

#pragma mark -
#pragma mark Make Cell Methods

+(ItinDetailsCellLabel*)makeCell:(UITableView*)tableView cellLabel:(NSString*)label cellValue:(NSString*)val
{
	ItinDetailsCellLabel *cell = (ItinDetailsCellLabel *)[tableView dequeueReusableCellWithIdentifier: ITIN_DETAILS_CELL_LABEL_REUSE_IDENTIFIER];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsCellLabel" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ItinDetailsCellLabel class]])
				cell = (ItinDetailsCellLabel *)oneObject;
	}
	
	[cell resetCellConfiguration];
	[cell.labelLabel setHidden:NO];
	[cell.labelValue setHidden:NO];
	
	cell.labelLabel.text = label;
	cell.labelValue.text = val;
	
	return cell;
}

+(ItinDetailsCellLabel*)makeVendorCell:(UITableView*)tableView vendor:(NSString*)vendorName
{
	ItinDetailsCellLabel *cell = (ItinDetailsCellLabel *)[tableView dequeueReusableCellWithIdentifier: ITIN_DETAILS_CELL_INFO_REUSE_IDENTIFIER];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsCellLabel" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ItinDetailsCellLabel class]])
				cell = (ItinDetailsCellLabel *)oneObject;
	}
	
	[cell resetCellConfiguration];
	[cell.labelVendor setHidden:NO];
	
	cell.labelVendor.text = vendorName;
	
	return cell;
}

+(ItinDetailsCellLabel*)makeLocationCell:(UITableView*)tableView location:(NSString*)location
{
	ItinDetailsCellLabel *cell = [self makeIconValueCell:tableView iconName:@"action_map" value:location];
	[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	return cell;
}

+(ItinDetailsCellLabel*)makePhoneCell:(UITableView*)tableView phoneNumber:(NSString*)phoneNumber
{
	ItinDetailsCellLabel *cell = [self makeIconValueCell:tableView iconName:@"action_phone" value:phoneNumber];
	return cell;
}

+(ItinDetailsCellLabel*)makeIconValueCell:(UITableView*)tableView iconName:(NSString*)iconName value:(NSString*)value
{
	//
	// This method is based on ItinDetailsViewController's tableView:cellForRowAtIndexPath
	//
	
	ItinDetailsCellLabel *cell = (ItinDetailsCellLabel *)[tableView dequeueReusableCellWithIdentifier: ITIN_DETAILS_CELL_LABEL_REUSE_IDENTIFIER];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsCellLabel" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ItinDetailsCellLabel class]])
				cell = (ItinDetailsCellLabel *)oneObject;
	}
	
	[cell resetCellConfiguration];
	[cell.labelValue setHidden:NO];
	[cell.ivIcon setHidden:NO];
	
	cell.ivIcon.image = [UIImage imageNamed:iconName];
	
	cell.labelValue.text = value;
	cell.labelValue.textColor = [UIColor blueColor];
	[cell.labelValue setTextAlignment:NSTextAlignmentLeft];
	[cell.labelValue setFont:[UIFont systemFontOfSize:15.0f]];
	
	return cell;
}


@end

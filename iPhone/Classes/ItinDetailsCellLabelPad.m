//
//  ItinDetailsCellLabelPad.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ItinDetailsCellLabelPad.h"
#import "WebViewController.h"


@implementation ItinDetailsCellLabelPad

@synthesize labelLabel;
@synthesize labelValue;
@synthesize imgView;
@synthesize labelVendor;
@synthesize labelValue1;
@synthesize labelValue2, labelValue3, labelValue4, labelValue5, labelValue6;
@synthesize specialValueWeb;
@synthesize specialValuePhone;
@synthesize btn1;
@synthesize btn2, ivBackground;
@synthesize btnCancel;

@synthesize rootVC;
@synthesize idVC;
@synthesize webViewTitle, ivIcon;
@synthesize lv1, lv2,lv3, lv4, lv5, lv6;
@synthesize lblShadow, lblWhiteBack;

NSString * const ITIN_DETAILS_PAD_CELL_LABEL_REUSE_IDENTIFIER = @"ItinDetailsCellLabelPad"; // TODO: Make consistent with reusable identifier in Interface Builder!

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
		webView.rootViewController = rootVC;
		webView.url = [NSString stringWithFormat:@"http://%@", specialValueWeb];
		webView.viewTitle = webViewTitle;
		[idVC presentViewController:webView animated:YES completion:nil]; 
		
	}
}

-(void)btnCancelPressed:(id)sender
{
    
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

+(ItinDetailsCellLabelPad*)makeCell:(UITableView*)tableView cellLabel:(NSString*)label cellValue:(NSString*)val
{
	ItinDetailsCellLabelPad *cell = (ItinDetailsCellLabelPad *)[tableView dequeueReusableCellWithIdentifier: ITIN_DETAILS_CELL_LABEL_REUSE_IDENTIFIER];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsCellLabelPad" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ItinDetailsCellLabelPad class]])
				cell = (ItinDetailsCellLabelPad *)oneObject;
	}
	
	[cell resetCellConfiguration];
	[cell.labelLabel setHidden:NO];
	[cell.labelValue setHidden:NO];
	
	cell.labelLabel.text = label;
	cell.labelValue.text = val;
	
	return cell;
}

+(ItinDetailsCellLabelPad*)makeVendorCell:(UITableView*)tableView vendor:(NSString*)vendorName
{
	ItinDetailsCellLabelPad *cell = (ItinDetailsCellLabelPad *)[tableView dequeueReusableCellWithIdentifier: ITIN_DETAILS_CELL_INFO_REUSE_IDENTIFIER];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsCellLabelPad" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ItinDetailsCellLabelPad class]])
				cell = (ItinDetailsCellLabelPad *)oneObject;
	}
	
	[cell resetCellConfiguration];
	[cell.labelVendor setHidden:NO];
	
	cell.labelVendor.text = vendorName;
	
	return cell;
}

+(ItinDetailsCellLabelPad*)makeLocationCell:(UITableView*)tableView location:(NSString*)location
{
	ItinDetailsCellLabelPad *cell = [self makeIconValueCell:tableView iconName:@"action_map" value:location];
	[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	return cell;
}

+(ItinDetailsCellLabelPad*)makePhoneCell:(UITableView*)tableView phoneNumber:(NSString*)phoneNumber
{
	ItinDetailsCellLabelPad *cell = [self makeIconValueCell:tableView iconName:@"action_phone" value:phoneNumber];
	return cell;
}

+(ItinDetailsCellLabelPad*)makeIconValueCell:(UITableView*)tableView iconName:(NSString*)iconName value:(NSString*)value
{
	//
	// This method is based on ItinDetailsViewController's tableView:cellForRowAtIndexPath
	//
	
	ItinDetailsCellLabelPad *cell = (ItinDetailsCellLabelPad *)[tableView dequeueReusableCellWithIdentifier: ITIN_DETAILS_CELL_LABEL_REUSE_IDENTIFIER];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailsCellLabelPad" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ItinDetailsCellLabelPad class]])
				cell = (ItinDetailsCellLabelPad *)oneObject;
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

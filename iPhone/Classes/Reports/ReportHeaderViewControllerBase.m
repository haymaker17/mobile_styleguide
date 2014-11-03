//
//  ReportHeaderViewControllerBase.m
//  ConcurMobile
//
//  Created by yiwen on 4/21/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportHeaderViewControllerBase.h"
#import "FormatUtils.h"
#import "ImageUtil.h"

@implementation ReportHeaderViewControllerBase
@synthesize lblName, lblAmount, lblLine1, lblLine2, img1, img2, img3;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}


- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle
- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


#pragma mark -
#pragma mark Report Header VC Methods 
-(void)drawHeaderRpt:(id)thisObj HeadLabel:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LabelLine1:(UILabel *)labelLine1 LabelLine2:(UILabel *)labelLine2 
	  Image1:(UIImageView *)image1 Image2:(UIImageView *)image2 Image3:(UIImageView *)image3
{
    ReportData* thisRpt = (ReportData*) thisObj;
    
	if ([self isApproving])
	{
		headLabel.text = thisRpt.employeeName;
        //MOB-6353 - Show claimed amount
		headLabelAmt.text = [FormatUtils formatMoney:thisRpt.totalClaimedAmount crnCode:thisRpt.crnCode];
		labelLine1.text = thisRpt.reportName;
        labelLine2.text = @"";
	}
	else 
	{
		headLabel.text = thisRpt.reportName;
        //MOB-6353 - Show claimed amount
		headLabelAmt.text = [FormatUtils formatMoney:thisRpt.totalClaimedAmount crnCode:thisRpt.crnCode];
        labelLine1.text = [CCDateUtilities formatDateToMMMddYYYFromString:thisRpt.reportDate];
		labelLine2.text = thisRpt.apvStatusName;
	}
    
    [UtilityMethods drawNameAmountLabelsOrientationAdjustedWithResize:headLabel AmountLabel:headLabelAmt LeftOffset:10 RightOffset:10 Width:self.view.frame.size.width];

    // Deal with images
    NSMutableArray* iconNames = [[NSMutableArray alloc] initWithCapacity:2];
    
    if(thisRpt.hasException != nil && [thisRpt.hasException isEqualToString:@"Y"])
	{
		BOOL showAlert = [thisRpt.severityLevel isEqualToString:@"ERROR"];
		if (showAlert) 
			[iconNames addObject:@"icon_redex"];
		else 
			[iconNames addObject:@"icon_yellowex"];
    }
    
    if (rpt.receiptImageAvailable != nil && [rpt.receiptImageAvailable isEqualToString:@"Y"])
    {
        [iconNames addObject:@"icon_receipt"];
    }
     //MOB-11325 - display Report ready to submit flag
    if (rpt.prepForSubmitEmpKey!=nil &&  [rpt.prepForSubmitEmpKey isEqualToString:@"Y"])
	{
        [iconNames addObject:@"icon_ready_submit_header"];
    }

    image1.image = [iconNames count]>0?[ImageUtil getImageByName:iconNames[0]] : nil;
    image2.image = [iconNames count]>1?[ImageUtil getImageByName:iconNames[1]] : nil;


}

-(void)drawHeaderEntry:(EntryData *)thisEntry HeadLabel:(UILabel *)headLabel 
        AmountLabel:(UILabel *)headLabelAmt 
        LabelLine1:(UILabel *)labelLine1 LabelLine2:(UILabel *)labelLine2 
        Image1:(UIImageView *)image1 Image2:(UIImageView *)image2 Image3:(UIImageView *)image3 
{
	NSString *amt = [FormatUtils formatMoney:thisEntry.transactionAmount crnCode:thisEntry.transactionCrnCode];
	headLabel.text = thisEntry.expName;
	headLabelAmt.text = amt;
	labelLine1.text = [CCDateUtilities formatDateToMMMddYYYFromString:thisEntry.transactionDate];
	
	labelLine2.text = [self getVendorString:thisEntry.vendorDescription WithLocation:(NSString*) thisEntry.locationName];
    
    [UtilityMethods drawNameAmountLabelsOrientationAdjusted:headLabel AmountLabel:headLabelAmt LeftOffset:10 RightOffset:10 Width:self.view.frame.size.width];

	NSArray* iconNames = [self getIconNames:thisEntry];
    
    image1.image = [iconNames count]>0?[ImageUtil getImageByName:iconNames[0]] : nil;
    image2.image = [iconNames count]>1?[ImageUtil getImageByName:iconNames[1]] : nil;
    image3.image = [iconNames count]>2?[ImageUtil getImageByName:iconNames[2]] : nil;
}

@end

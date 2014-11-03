//
//  SubmitNeedReceiptsViewController.m
//  ConcurMobile
//
//  Created by yiwen on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SubmitNeedReceiptsViewController.h"
#import "ReportApprovalListCell.h"
#import "EntryData.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "ExSystem.h" 
#import "UserConfig.h"

@implementation SubmitNeedReceiptsViewController

@synthesize entryListView, entryList, lblSubmitConfirm, lblReceiptRequired, lblHowToProvide;
@synthesize btnSubmit, btnCancel;
@synthesize delegate = _delegate;
@synthesize lblSubmitCustomText, rpt, scrollView;

#pragma mark -
#pragma mark ViewController Methods
- (void)viewDidAppear:(BOOL)animated 
{
	[super viewDidAppear:animated];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
//	titleBtn.title = [Localizer getViewTitle:[self getViewIDKey]];
	
//	[tableView setBackgroundColor:[UIColor whiteColor]];

    // MOB-7870, 8370 (MWS) check and use custom confirmation msg.
    ExpenseConfirmation *submitConf = [[UserConfig getSingleton] submitConfirmationForPolicy:self.rpt.polKey];
    NSString *customAlertTitle = nil;
    NSString *customAlertMessage = nil;
    if (submitConf != nil && (submitConf.title != nil && submitConf.text != nil))
    {
        if ([submitConf.title length])
            customAlertTitle = submitConf.title;
        if ([submitConf.text length])
            customAlertMessage = submitConf.text;

        self.lblSubmitConfirm.text = customAlertTitle;
        self.lblSubmitCustomText.text = customAlertMessage;
    
        float w = self.view.frame.size.width - 40;
        if ([UIDevice isPad])
            w = 480-40; // Anticipate iPad resize to minimize empty space on top & bottom.
        
        CGSize maxSz = CGSizeMake(w, 640);
        CGSize s = [customAlertMessage sizeWithFont:lblSubmitCustomText.font constrainedToSize:maxSz];

        // Adjust label to current size, let auto resize adjust to iPhone or iPad
        self.lblSubmitCustomText.frame = CGRectMake(20, 0, self.view.frame.size.width - 40, s.height);
        CGSize rrSz = self.lblReceiptRequired.frame.size;
        self.lblReceiptRequired.frame = CGRectMake(20, s.height+2, rrSz.width, rrSz.height);
        CGSize hpSz = self.lblHowToProvide.frame.size;
        self.lblHowToProvide.frame = CGRectMake(20, self.lblReceiptRequired.frame.origin.y+2+rrSz.height, hpSz.width, hpSz.height);
        
        scrollView.contentSize = CGSizeMake(320, self.lblHowToProvide.frame.origin.y + hpSz.height +2);
        [scrollView showsVerticalScrollIndicator];
        [scrollView flashScrollIndicators];

    }
    else 
    {
        self.lblSubmitConfirm.text = [Localizer getLocalizedText:@"CONFIRM_REPORT_SUBMISSION"];
    }
  	
    self.lblReceiptRequired.text = [Localizer getLocalizedText:@"MSG_RECEIPT_REQUIRED"];
    if ([self.howToProvideMsgType isEqualToString:@"entriesNeedReceipt"])
        self.lblHowToProvide.text = [Localizer getLocalizedText:@"SUBMIT_HOW_TO_RECEIPT_MSG"];
    else if ([self.howToProvideMsgType isEqualToString:@"entriesNeedPaperRecipt"])
        self.lblHowToProvide.text = [Localizer getLocalizedText:@"SUBMIT_REQUIRE_PAPER_RECEIPT"];

    self.btnCancel.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
    self.btnSubmit.title = [Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"];
    
	[super viewDidLoad];

	[self.view setBackgroundColor:[UIColor colorWithRed:0.882871 green:0.887548 blue:0.892861 alpha:1]];
	
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[entryListView reloadData];
}


#pragma mark -
#pragma mark Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return entryList == nil? 0 : [entryList count];
}

// Similar to ApproveReportViewControllerBase.makeEntryCell, but only draw the first two lines.
-(void)makeEntryCell:(ReportApprovalListCell *)cell Entry:(EntryData *)entry
{	
	cell.lblAmount.font = [UIFont boldSystemFontOfSize:14];
	cell.lblName.font = [UIFont boldSystemFontOfSize:14];
	cell.lblName.text = entry.expName;
	cell.lblAmount.text = [FormatUtils formatMoney:entry.transactionAmount crnCode:entry.transactionCrnCode];
	cell.lblLine1.text = [DateTimeFormatter formatLocalDateMedium:entry.transactionDate];
	cell.lblLine2.text = nil;
    // MOB-6258 (iPhone), 6069 (iPad)
    [UtilityMethods drawNameAmountLabelsOrientationAdjustedWithResize:cell.lblName AmountLabel:cell.lblAmount LeftOffset:10 RightOffset:10 Width:cell.contentView.frame.size.width];

    cell.lblLine1.frame = CGRectMake(10, cell.lblLine1.frame.origin.y, cell.lblLine1.frame.size.width, cell.lblLine1.frame.size.height);
	[cell setAccessoryType:UITableViewCellAccessoryNone];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
   
	NSUInteger row = [indexPath row];
	
    // Configure the cell...
	ReportApprovalListCell *cell = (ReportApprovalListCell *)[tableView dequeueReusableCellWithIdentifier: @"ReportApprovalListCell"];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ReportApprovalListCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ReportApprovalListCell class]])
				cell = (ReportApprovalListCell *)oneObject;
	}
	
	EntryData *entry = (EntryData *) entryList[row];
	
	[self makeEntryCell:cell Entry:entry];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 40;
}


#pragma mark -
#pragma mark SubmitNeedReceipts Delegator Methods
-(void)buttonCancelPressed:(id)sender
{
	[_delegate cancelSubmitAfterReceipts];	
}

-(void)buttonSubmitPressed:(id)sender
{
	[_delegate confirmSubmitAfterReceipts];	
}


#pragma mark -
#pragma mark Memory management
- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}

- (void)dealloc {
	self.delegate = nil;
}

@end


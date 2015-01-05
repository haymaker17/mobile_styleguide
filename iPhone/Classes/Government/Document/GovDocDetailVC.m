//
//  GovDocDetailVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/7/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocDetailVC.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "GovDocumentDetailData.h"
#import "DrillCell.h"
#import "GovDocPerDiemVC.h"
#import "GovDocAcctVC.h"
#import "GovDocExpensesVC.h"
#import "GovDocTotalsVC.h"
#import "GovDocExceptionsVC.h"
#import "GovDocCommentVC.h"
#import "GovDocDetailDrillCell.h"
#import "ImageUtil.h"
#import "GovDocStampVC.h"
#import "ReceiptEditorVC.h"
#import "GovAttachReceiptData.h"
#import "ReceiptCache.h"
#import "HotelBookingCell.h"
#import "ListViewController.h"

#define   kSection_Receipt @"Receipt"
#define   kSection_TripTypeCode @"Trip Type Code"
#define   kSection_PerDiem @"Per Diem Locations"
#define   kSection_Expenses @"Expenses"
#define   kSection_Accounting @"Accounting Allocation"
#define   kSection_Totals @"Totals and Travel Advances"
#define   kSection_Audits @"Audits"
#define   kSection_Comments @"Comments"

@interface GovDocDetailVC(Private)

-(void)initSections;
-(void)drawHeader;
-(IBAction) actionStamp:(id)sender;
-(void)setupToolbar;

@end

@implementation GovDocDetailVC

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    if (self.doc == nil)
        [self showLoadingView];
    
    [self setupToolbar];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:NO];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];
    if ([msg.idKey isEqualToString:GOV_DOCUMENT_DETAIL])
	{
        if ([self isViewLoaded])
        {
            [self hideLoadingView];
            [self hideWaitView];
        }
        
        if (msg.errBody != nil)
        {
            MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Unable to add expense"]
                                  message:msg.errBody
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            
            [alert show];
        }
        else
        {
            GovDocumentDetailData * resp = (GovDocumentDetailData*)msg.responder;
            self.doc = resp.currentDoc;
            if ([self.doc.receiptId lengthIgnoreWhitespace])
            {
                self.receipt = [[Receipt alloc] init];
                self.receipt.receiptId = self.doc.receiptId;
                self.receipt.dataType = @"pdf";
            }
            
            if (self.doc.tripTypeCodes != nil)
            {
                for (GovDocTripTypeCode *oneRow in self.doc.tripTypeCodes)
                {
                    if ([oneRow.selected boolValue]) {
                        self.selectedTripTypeCode = oneRow;
                    }
                }
            }

            [self initSections];
            [self drawHeader];
            [self setupToolbar];
            [self.tableList reloadData];

            NSDictionary *param = @{@"Type": self.doc.docType, @"Audit Count": [NSNumber numberWithFloat:[self.doc.auditFailed floatValue] + [self.doc.auditPassed floatValue]]};
            [Flurry logEvent:@"Document Detail: View" withParameters:param];
        }
    }
    else if ([msg.idKey isEqualToString:GOV_ATTACH_RECEIPT])
    {
        GovAttachReceiptData* data = (GovAttachReceiptData*) msg.responder;
        if (msg.errBody != nil && ![data.status.status isEqualToString:@"SUCCESS"])
        {
            NSString * errMsg = msg.errBody == nil? data.status.errMsg : msg.errBody;
            
            [self hideWaitView];
            UIAlertView *alert = [[MobileAlertView alloc]
                                           initWithTitle:[Localizer getLocalizedText:@"Unable to attach receipt to document"]
                                           message:errMsg
                                           delegate:nil
                                           cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                           otherButtonTitles:nil];
            
            [alert show];
        }
        else
        {
            if (self.doc.receiptId == nil)
            {
                // Refresh doc details with new receipt id and audits
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             self.doc.travelerId, @"TRAVELER_ID",
                                             self.doc.docType, @"DOC_TYPE",
                                             self.doc.docName, @"DOC_NAME",
                                             nil];
                [[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENT_DETAIL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            }
            else
            {
                // Need to refetch the document level receipt
                [self hideWaitView];
                [[ReceiptCache sharedInstance] deleteReceiptsMatchingId:self.doc.receiptId];
            }
        }
    }
}

-(void)initSections
{
    self.sections = [[NSMutableArray alloc] initWithCapacity:3];
    
    NSMutableArray *section0 = [[NSMutableArray alloc] initWithCapacity:1];
    [section0 addObject:kSection_TripTypeCode];
    [self.sections addObject:section0];
    
    // MOB-19519: Remove document level image attachment capabilities
//    NSMutableArray* section1 = [[NSMutableArray alloc] initWithCapacity:1];
//    [section1 addObject:kSection_Receipt];
//    [sections addObject:section1];
    
    NSMutableArray* section2 = [[NSMutableArray alloc] initWithCapacity:7];
    if (self.doc.perdiemTDY != nil && [self.doc.perdiemTDY count]>0)
    {
        [section2 addObject:kSection_PerDiem];
    }
    
    [section2 addObject:kSection_Expenses];
    [section2 addObject:kSection_Accounting];
    [section2 addObject:kSection_Totals];
    [section2 addObject:kSection_Audits];
    [section2 addObject:kSection_Comments];
    [self.sections addObject:section2];
}

-(NSString*) getMenuId:(NSIndexPath*) ixPath
{
    NSArray* sec = [self.sections objectAtIndex: ixPath.section];
    return [sec objectAtIndex:ixPath.row];
}

-(void)drawHeader
{
    GovDocumentDetail * thisDoc = (GovDocumentDetail*) self.doc;
    
    self.lblName.text = thisDoc.travelerName;
    self.lblAmount.text = [FormatUtils formatMoneyWithNumber:thisDoc.totalEstCost crnCode:@"USD"];
    self.lblDocName.text = thisDoc.docName;
    self.lblDocType.text = thisDoc.docTypeLabel;
    NSString *startFormatted = [DateTimeFormatter formatDate:thisDoc.tripBeginDate Format:@"MMM dd, yyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    NSString *endFormatted = [DateTimeFormatter formatDate:thisDoc.tripEndDate Format:@"MMM dd, yyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    if(!(startFormatted == nil || endFormatted == nil))
        self.lblDates.text = [NSString stringWithFormat:@"%@ - %@", startFormatted, endFormatted];
    else
        self.lblDates.text = @"";
    
	if (true/*[self isAuth]*/)
	{
        self.lblVal1.text = thisDoc.currentStatus;
        self.lblVal2.text = thisDoc.tANumber;
        self.lblVal3.text = thisDoc.purposeCode;
        self.lblVal4.text = thisDoc.emissionsLbs.stringValue;//[NSString stringWithFormat:@"%d",[thisDoc.emissionsLbs intValue]];
	}
//	else
//	{
//	}
//    
    [UtilityMethods drawNameAmountLabelsOrientationAdjustedWithResize:self.lblName AmountLabel:self.lblAmount LeftOffset:10 RightOffset:10 Width:self.view.frame.size.width];
    
    // Deal with images
    NSMutableArray* iconNames = [[NSMutableArray alloc] initWithCapacity:2];
    
    if([self.doc.exceptions count] >0)
	{
			[iconNames addObject:@"icon_redex"];
//		else
//			[iconNames addObject:@"icon_yellowex"];
    }
    
    if ([self.doc.receiptId lengthIgnoreWhitespace])
    {
        [iconNames addObject:@"icon_receipt_19"];
    }
    
    self.img1.image = [iconNames count]>0?[ImageUtil getImageByName:[iconNames objectAtIndex:0]] : nil;
    self.img2.image = [iconNames count]>1?[ImageUtil getImageByName:[iconNames objectAtIndex:1]] : nil;
    
    
}

-(IBAction) actionStamp:(id)sender
{
    GovDocStampVC *c = [[GovDocStampVC alloc] initWithNibName:@"GovDocStampVC" bundle:nil];
    
    [c setSeedData:self.doc];
	[self.navigationController pushViewController:c animated:YES];
}

-(void)setupToolbar
{
	if(![ExSystem connectedToNetwork])
    {
        [self makeOfflineBar];
    }
    else
	{
		UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
		flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];

		NSMutableArray *toolbarItems = [NSMutableArray arrayWithCapacity:5];
		
		if (self.doc != nil && (self.doc.needsStamping != nil && [self.doc.needsStamping boolValue]==TRUE))
        {
//            SEL leftSelector = @selector(actionReturn:);
//            NSString* leftBtnLabel = [Localizer getLocalizedText:@"Return"];
//            UIBarButtonItem *btnLeft = [[UIBarButtonItem alloc] initWithTitle:leftBtnLabel style:UIBarButtonItemStyleBordered target:self action:leftSelector];
//            [toolbarItems addObject:btnLeft];
            
            SEL rightSelector = @selector(actionStamp:);
            
            NSString* rightBtnLabel = [Localizer getLocalizedText:@"Stamp"];
            
            [toolbarItems addObject:flexibleSpace];
            
            UIBarButtonItem *btnRight = [[UIBarButtonItem alloc] initWithTitle:rightBtnLabel style:UIBarButtonItemStyleBordered target:self action:rightSelector];
            [toolbarItems addObject:btnRight];
		}
        //	[rootViewController.navigationController.toolbar setHidden:NO];
        [self.navigationController setToolbarHidden:NO];
		[self setToolbarItems:toolbarItems animated:YES];
	}
    
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.sections count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [[self.sections objectAtIndex:section] count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString* menuId = [self getMenuId:indexPath];

    UITableViewCell *cell = nil;
    if ([menuId isEqualToString:kSection_Receipt])
    {
        NSString *command = [Localizer getLocalizedText:@"View Receipt"];
        // receipt cell
        if (self.receipt==nil && ![self.receipt hasReceipt] && [ExSystem connectedToNetwork])
        {
            command = [Localizer getLocalizedText:@"Add Receipt"];
        }
        cell = [DrillCell makeDrillCell:tableView withText:command withImage:@"icon_receipt_button" enabled:YES];
    }
    else if ([menuId isEqualToString:kSection_TripTypeCode])
    {
        HotelBookingCell *hbCell = (HotelBookingCell*) [tableView dequeueReusableCellWithIdentifier:@"HotelBookingSingleCell"];
        if (hbCell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelBookingSingleCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[HotelBookingCell class]])
                    hbCell = (HotelBookingCell *)oneObject;
        }

        NSString *label = [Localizer getLocalizedText:@"Type code (Trip Type)"];
        NSString *value = [self.selectedTripTypeCode.selected boolValue] ? self.selectedTripTypeCode.tripType : [Localizer getLocalizedText:@"Please specify *"];

        hbCell.lblLabel.text = label;
        hbCell.lblValue.text = value;

        if ([hbCell.lblValue.text isEqualToString:[Localizer getLocalizedText:@"Please specify *"]] && self.doc.requireTypeCode)
        {
            hbCell.lblValue.textColor = [UIColor redColor];
        }
        else
        {
            hbCell.lblValue.textColor = [UIColor blackColor];
        }
        
        [hbCell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        
        cell = hbCell;
    }
    else
    {
        GovDocDetailDrillCell *gdddCell = [tableView dequeueReusableCellWithIdentifier:@"GovDocDetailDrillCell"];
        if (gdddCell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"GovDocDetailDrillCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[GovDocDetailDrillCell class]])
                    gdddCell = (GovDocDetailDrillCell *)oneObject;
        }
        
        if ([menuId isEqualToString:kSection_PerDiem])
        {
            gdddCell.textLabel.text = [kSection_PerDiem localize];
            gdddCell.detailTextLabel.text = [NSString stringWithFormat:@"(%lu)", (unsigned long)[self.doc.perdiemTDY count]];
        }
        else if ([menuId isEqualToString:kSection_Expenses])
        {
            gdddCell.textLabel.text = [kSection_Expenses localize];
            gdddCell.detailTextLabel.text = [NSString stringWithFormat:@"(%lu)", (unsigned long)[self.doc.expenses count]];
        }
        else if ([menuId isEqualToString:kSection_Accounting])
            gdddCell.textLabel.text = [kSection_Accounting localize];
        else if ([menuId isEqualToString:kSection_Totals])
            gdddCell.textLabel.text = [kSection_Totals localize];
        else if ([menuId isEqualToString:kSection_Audits])
        {
            gdddCell.textLabel.text = [kSection_Audits localize];
            gdddCell.detailTextLabel.text = [NSString stringWithFormat:@"%@ (%d) %@ (%d)", [@"Pass" localize], [self.doc.auditPassed intValue], [@"Fail" localize], [self.doc.auditFailed intValue]];
        }
        else //if (indexPath.row == 6)
            gdddCell.textLabel.text = [kSection_Comments localize];
        
        
        CGSize s = [gdddCell.textLabel.text sizeWithFont:gdddCell.textLabel.font];
        CGRect f = gdddCell.detailTextLabel.frame;
        float x = gdddCell.textLabel.frame.origin.x + s.width + 5;
        gdddCell.detailTextLabel.frame = CGRectMake(x, f.origin.y, f.size.width, f.size.height);
        //gdddCell.detailTextLabel.textAlignment = NSTextAlignmentLeft;
        
        [gdddCell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        cell = gdddCell;
    }
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString* menuId = [self getMenuId:indexPath];

    if ([menuId isEqualToString:kSection_TripTypeCode])
    {
        return 62;
    }

    return 40;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString* menuId = [self getMenuId:indexPath];

    if ([menuId isEqualToString:kSection_Receipt])
    {
        [self showReceiptViewer];
        
        NSDictionary *param = @{@"Type" : @"Receipt"};
        [Flurry logEvent:@"Drill-in Action" withParameters:param];
    }
    
    if ([menuId isEqualToString:kSection_TripTypeCode])
    {
        [self showTripTypeCode];
    }
    
    if ([menuId isEqualToString:kSection_PerDiem])
    {
        [GovDocPerDiemVC showDocPerDiem:self withDoc:self.doc];
        
        NSDictionary *param = @{@"Type" : @"Per Diem Locations"};
        [Flurry logEvent:@"Drill-in Action" withParameters:param];
    }
    else if ([menuId isEqualToString:kSection_Expenses])
    {
        [GovDocExpensesVC showDocExpenses:self withDoc:self.doc];
        
        NSDictionary *param = @{@"Type" : @"Expenses"};
        [Flurry logEvent:@"Drill-in Action" withParameters:param];
        
        int count = 0;
        for (GovDocExpense *expense in self.doc.expenses)
        {
            if ( [expense.imageId length] )
                count++;
        }
        param = @{@"Count" : [NSNumber numberWithInteger:[self.doc.expenses count]], @"Count with receipt" : [NSNumber numberWithInt:count]};;
        [Flurry logEvent:@"Document Detail: Expenses" withParameters:param];
    }
    else if ([menuId isEqualToString:kSection_Accounting])
    {
        [GovDocAcctVC showDocAccts:self withDoc:self.doc];
        
        NSDictionary *param = @{@"Type" : @"Accounting"};
        [Flurry logEvent:@"Drill-in Action" withParameters:param];
    }
    else if ([menuId isEqualToString:kSection_Totals])
    {
        [GovDocTotalsVC showDocTotals:self withDoc:self.doc];
        
        NSDictionary *param = @{@"Type" : @"Totals and Travel Advances"};
        [Flurry logEvent:@"Drill-in Action" withParameters:param];
    }
    else if ([menuId isEqualToString:kSection_Audits])
    {
        [GovDocExceptionsVC showDocExceptions:self withDoc:self.doc];
        
        NSDictionary *param = @{@"Type" : @"Audits"};
        [Flurry logEvent:@"Drill-in Action" withParameters:param];
    }
    else if ([menuId isEqualToString:kSection_Comments])
    {
        [GovDocCommentVC showDocComment:self withComment:self.doc.comments];
        
        NSDictionary *param = @{@"Type" : @"Comments"};
        [Flurry logEvent:@"Drill-in Action" withParameters:param];
    }
    
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}


#pragma mark -
#pragma mark Receipt Action
- (void)showReceiptViewer
{
	ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"Receipt"];
    receiptView.delegate = self;
    receiptView.canDelete = NO;
    receiptView.canUpdate = YES;
    [receiptView setSeedData:self.receipt]; // Pass in receipt object to fill the downloaded image data.
    [self.navigationController pushViewController:receiptView animated:YES];
}

//-(BOOL) isReceiptUpdated
//{
//    // TODO - for existing expense, need to compare with existing receipt id
//    return self.receipt != nil && [self.receipt.receiptId length];
//}

#pragma mark ReceiptEditorDelegate
-(void) receiptUpdated:(Receipt*) rcpt useV2Endpoint:(BOOL)useV2Endpoint
{
    if (rcpt != nil && rcpt.receiptId != nil)
    {
        // MOB-12200 creating new receipt object and only use receiptID.
        // This fix "rcpt", "self.receipt" share same memory address. Cause new rcpt.receiptId getting override with old one.
        NSString *newReceiptId = rcpt.receiptId;
        if (![rcpt.receiptId isEqualToString:self.doc.receiptId] && self.doc.receiptId != nil)
        {
            // Reset - need to refresh the document level receipt.
            self.receipt.receiptId = self.doc.receiptId;
            self.receipt.pdfData = nil;
            self.receipt.receiptImg = nil;
        }
        // send out receipt attaching message
        [self showWaitView];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                     newReceiptId, @"RECEIPT_ID",
                                     self.doc.docName, @"DOC_NAME",
                                     self.doc.docType, @"DOC_TYPE",
                                     nil];
        [[ExSystem sharedInstance].msgControl createMsg:GOV_ATTACH_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    // How to handle wait view, etc...
//    //    self.isDirty = true;  // TODO - use a different flag for receipt dirty, or new receipt vs orig receipt?
}

-(void) receiptDeleted:(Receipt*) receipt{}
-(void) receiptQueued:(Receipt*) receipt{}  // For offline?

- (NSArray *) getListItems
{
    NSMutableArray* result = [[NSMutableArray alloc] init];
    for (GovDocTripTypeCode *code in self.doc.tripTypeCodes)
    {
        ListItem * li = [[ListItem alloc] init];
        li.liKey = code.typeCode;
        li.liName = code.tripType;
        [result addObject:li];
    }
    return result;
}

- (NSIndexPath *) getDefaultSelectedIdxPath
{
    NSIndexPath *idx = nil;
    for (int i = 0; i < [self.doc.tripTypeCodes count]; i++) {
        GovDocTripTypeCode *oneItem = self.doc.tripTypeCodes[i];
        if ([oneItem.selected boolValue])
        {
            idx = [NSIndexPath indexPathForRow:i inSection:0];
        }
    }
    return idx;
}

-(void) showTripTypeCode
{
    ListViewController *vc = [[ListViewController alloc] initWithNibName:@"ListView" bundle:nil];
    vc.dataSourceArray = [self getListItems];
    vc.defaultSelectedIdxPath = [self getDefaultSelectedIdxPath];
    vc.title = [Localizer getLocalizedText:@"Please specify"];
    vc.delegate = self;
    
    if([UIDevice isPad])
		vc.modalPresentationStyle = UIModalPresentationFormSheet;
	[self.navigationController pushViewController:vc animated:YES];
}

#pragma mark view display
+(void)showAuthFromRootWithDocName:(NSString*) docName withDocType:(NSString*) docType
{
	GovDocDetailVC *c = [[GovDocDetailVC alloc] initWithNibName:@"GovDocDetailVC" bundle:nil];
    c.title = [@"Authorization" localize];

    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
//								 travId, @"TRAVELER_ID",
								 docType, @"DOC_TYPE",
								 docName, @"DOC_NAME",
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENT_DETAIL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:c];
    UIViewController * pvc = (UIViewController*) [ConcurMobileAppDelegate findHomeVC];
    [pvc.navigationController popToRootViewControllerAnimated:NO];
	[pvc.navigationController pushViewController:c animated:YES];
}

+(void)pushAuthWithDocName:(NSString*) docName withDocType:(NSString*) docType
{
	GovDocDetailVC *c = [[GovDocDetailVC alloc] initWithNibName:@"GovDocDetailVC" bundle:nil];
    c.title = [@"Authorization" localize];
    
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 //								 travId, @"TRAVELER_ID",
								 docType, @"DOC_TYPE",
								 docName, @"DOC_NAME",
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENT_DETAIL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:c];
    UIViewController * pvc = (UIViewController*) [ConcurMobileAppDelegate findHomeVC];
    
//    [pvc.navigationController popToRootViewControllerAnimated:NO];
	[pvc.navigationController pushViewController:c animated:YES];
}

+(void)showDocDetail:(UIViewController*)pvc withTraveler:(NSString*)travId withDocName:(NSString*) docName withDocType:(NSString*) docType withGtmDocType:(NSString*) gtmDocType
{
	GovDocDetailVC *c = [[GovDocDetailVC alloc] initWithNibName:@"GovDocDetailVC" bundle:nil];
    c.title = [Localizer getLocalizedText:([gtmDocType isEqualToString:@"VCH"]?@"Voucher":@"Authorization")];

//    [c setSeedData:report role:curRole];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 travId, @"TRAVELER_ID",
								 docType, @"DOC_TYPE",
								 docName, @"DOC_NAME",
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENT_DETAIL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:c];
	
	[pvc.navigationController pushViewController:c animated:YES];
}

#pragma mark - OptionsSelectDelegate functions
-(void) optionSelected:(NSObject*)obj withIdentifier:(NSObject*) identifier
{
    self.selectedTripTypeCode = (GovDocTripTypeCode *)obj;
    [self.tableList reloadData];
}

-(void) optionSelectedAtIndex:(NSInteger)row withIdentifier:(NSObject*) identifier
{
    self.selectedTripTypeCode = self.doc.tripTypeCodes[row];
    self.selectedTripTypeCode.selected = [NSNumber numberWithBool:YES];
    NSString *selectedTripType = (NSString *)identifier;
    if (self.selectedTripTypeCode.tripType != selectedTripType)
    {
        DLog(@"Trip Type datasource corrupt.");
    }
    [self.tableList reloadData];
}
@end

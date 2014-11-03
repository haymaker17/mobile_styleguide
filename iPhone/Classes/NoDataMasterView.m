//
//  NoDataMasterView.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 4/28/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "NoDataMasterView.h"
#import "ReceiptStoreListView.h"
#import "ReportAttendeesViewController.h"

#define ICON_Y_OFFSET 40
#define VERTICAL_SPACING 5.0
#define TITLE_LBL_HEIGHT 40.0
#define TOOLBAR_HEIGHT  44.0

@interface NoDataMasterView (Private)
- (NSString*)lookUpTitle:(NSString*)key;
- (void)setTitleForTitleLbl;
- (NSString*)lookUpImageName:(NSString*)key;
- (void)setIconForImageView;
- (NSString*)lookUpButtonTitle:(NSString*)key;
- (void)setTitleForButton;
- (BOOL)adjustForSpecialViews;
- (void)setInstrcuctionForLbl;
@end

@implementation NoDataMasterView
@synthesize titleLbl;
@synthesize iconImg;
@synthesize actionBtn;
@synthesize delegate;
@synthesize instructionLbl;

- (id)initWithNib
{
    // Initialization code
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"NoDataMasterView" 
                                                 owner:self 
                                               options:nil];
    
    NoDataMasterView *viewFromXib = (NoDataMasterView*)nib[0];
    self = viewFromXib;
    return self;
}

- (void)prepareSubviews
{
    MobileViewController *mvcDelegate = (MobileViewController*)delegate;
    
    [self setTitleForTitleLbl];
    [self setIconForImageView];
    [self setInstrcuctionForLbl];
    if (mvcDelegate != nil ) 
    {
        BOOL doesAllowActionWhileOffline = NO;
        if (![ExSystem connectedToNetwork] || mvcDelegate.baseViewState == VIEW_STATE_OFFLINE)
        {
            if ([delegate respondsToSelector:@selector(allowActionWhileOffline)])
            {
                doesAllowActionWhileOffline = [delegate allowActionWhileOffline];
            }
        }
        
        if (mvcDelegate.baseViewState == VIEW_STATE_NEGATIVE || mvcDelegate.baseViewState == VIEW_STATE_OFFLINE) {
            if ([ExSystem connectedToNetwork] || doesAllowActionWhileOffline)
            {
                NSString *imgName = @"blue_pill_button";
                [actionBtn setBackgroundImage:[UIImage imageNamed:imgName] forState:UIControlStateNormal];
                [actionBtn setUserInteractionEnabled:YES];
                [self setTitleForButton];
            }
            else
            {
                [actionBtn setUserInteractionEnabled:NO];
                [actionBtn setHidden:YES];
            }
        }
    }
    BOOL hideToolBar = YES;
    hideToolBar = [self adjustForSpecialViews];
    
    if (hideToolBar) 
    {
        if (mvcDelegate != nil && mvcDelegate.navigationController.toolbar != nil) {
            [mvcDelegate.navigationController setToolbarHidden:YES];
            float width = self.frame.size.width;
            float height = self.frame.size.height;
            self.frame = CGRectMake(0, 0, width, height + TOOLBAR_HEIGHT);
        }
    }
}

- (BOOL)adjustForSpecialViews
{
    if ([[(MobileViewController*)delegate getViewIDKey] isEqualToString:@"REPORT_DETAIL"])
    { 
        iconImg.frame = CGRectMake(iconImg.frame.origin.x, ICON_Y_OFFSET, iconImg.frame.size.width, iconImg.frame.size.height);
        titleLbl.frame = CGRectMake(titleLbl.frame.origin.x, iconImg.frame.origin.y + iconImg.frame.size.height + VERTICAL_SPACING , titleLbl.frame.size.width, TITLE_LBL_HEIGHT);
        actionBtn.frame = CGRectMake(actionBtn.frame.origin.x, titleLbl.frame.origin.y + titleLbl.frame.size.height + VERTICAL_SPACING, actionBtn.frame.size.width, actionBtn.frame.size.height);
        instructionLbl.frame = CGRectMake(actionBtn.frame.origin.x, actionBtn.frame.origin.y + actionBtn.frame.size.height+VERTICAL_SPACING, 0, 0);
        return NO;
    }
    if ([[(MobileViewController*)delegate getViewIDKey] isEqualToString:@"RECEIPT_DETAIL_VIEW"]/*|| MOB-8656
        [[(MobileViewController*)delegate getViewIDKey] isEqualToString:@"RECEIPT_STORE_VIEWER"]*/)
    { 
        return NO;
    }
    else
    {
        return [(MobileViewController*)delegate adjustNoDataView:self];
    }
}

- (IBAction)actionButtonPressed:(id)sender
{
    if (self.delegate != nil && [delegate respondsToSelector:@selector(actionOnNoData:)]) {
        [delegate actionOnNoData:sender];
    }
}

- (void)setInstrcuctionForLbl
{    
    MobileViewController *mvcDelegate = (MobileViewController*)delegate;
    NSString * instructionStr = nil;
    if (mvcDelegate != nil && (mvcDelegate.baseViewState == VIEW_STATE_NEGATIVE || mvcDelegate.baseViewState == VIEW_STATE_OFFLINE))
    {
        if ([mvcDelegate respondsToSelector:@selector(instructionForNoDataView)])
        {
            instructionStr = [mvcDelegate instructionForNoDataView];
        }
    }

    if (![instructionStr length])
        [self.instructionLbl setHidden:YES];
    else
    {
        [self.instructionLbl setText:instructionStr];
        [self.instructionLbl setHidden:NO];
    }

}

- (void)setTitleForTitleLbl
{    
    MobileViewController *mvcDelegate = (MobileViewController*)delegate;
    if (mvcDelegate != nil)
    {
        NSString * titleStr = nil;
        if (mvcDelegate.baseViewState == VIEW_STATE_NEGATIVE) {
            titleStr = [self lookUpTitle:[(MobileViewController*)delegate getViewIDKey]];
        }
        else if (mvcDelegate.baseViewState == VIEW_STATE_OFFLINE)
        {
            titleStr = [Localizer getLocalizedText:@"Offline"];
        }
        [self.titleLbl setText:titleStr];
    }
    else
    {
        [self.titleLbl setText:nil];
    }
}

- (void)setIconForImageView
{
    NSString *imgName = [self lookUpImageName:[(MobileViewController*)delegate getViewIDKey]];
    [iconImg setImage:[UIImage imageNamed:imgName]];
}

- (void)setTitleForButton
{
    NSString *btnTitle = [self lookUpButtonTitle:[(MobileViewController*)delegate getViewIDKey]];
    if (![btnTitle length]) {
        [actionBtn setHidden:YES];
    }
    else {
        [actionBtn setTitle:btnTitle forState:UIControlStateNormal];
    }
}

#pragma mark -
#pragma mark LookUp methods
- (NSString*)lookUpTitle:(NSString*)key
{
    if (![ExSystem connectedToNetwork])
    {
        if ([delegate respondsToSelector:@selector(canShowOfflineTitleForNoDataView)])
        {
            if ([delegate canShowOfflineTitleForNoDataView])
                return [Localizer getLocalizedText:@"Offline"];
        }
    }
    
    if ([delegate respondsToSelector:@selector(titleForNoDataView)])
    {
        return [delegate titleForNoDataView];
    }
    else if ([key isEqualToString:@"ACTIVE_ENTRIES"] || [key isEqualToString:@"REPORT_DETAIL"]) {
        return [Localizer getLocalizedText:@"No Expenses"];
    }
    else if ([key isEqualToString:@"AIRSHOPFILTER"] || [key isEqualToString:@"AIRSHOPSUMMARY"]) 
        return [Localizer getLocalizedText:@"No Flights Found"];
    else if ([key isEqualToString:@"INVOICE_RECEIPT"]) {
        return [Localizer getLocalizedText:@"NO_RECEIPT_FOUND"];
    }
    else if ([key isEqualToString:@"RECEIPT_STORE_VIEWER"]) {
        return [Localizer getLocalizedText:@"NO_RECEIPTS_NEG"];
    }
    // MOB-10896 : Dont set the text here. Let delegate call back to ReceiptDetailViewController.m
    else if ([key isEqualToString:@"RECEIPT_STORE_DETAIL_VIEW"]) { 
        return [Localizer getLocalizedText:@"NO_RECEIPT_NEG"];
    }
    else if ([key isEqualToString:@"ACTIVE_REPORTS"] || [key isEqualToString:@"APPROVE_REPORTS"]) {
        return [Localizer getLocalizedText:@"No Reports"];
    }
    else if ([key isEqualToString:@"TRIPS_LIST_NAVI"]) {
        return [Localizer getLocalizedText:@"No Trips"];
    }
    else if ([key isEqualToString:@"INVOICE_LINEITEMS"]) {
        return [Localizer getLocalizedText:@"MSG_NO_INVOICE_DETAIL"];
    }
    else if ([key isEqualToString:@"ITEMIZATION_LIST"]) {
        return [Localizer getLocalizedText:@"No Itemization"];
    }
    else if ([key isEqualToString:@"ITEMIZATION_LIST"]) {
        return [Localizer getLocalizedText:@"No Itemization"];
    }
    else if ([key isEqualToString:@"COMMENT_LIST"]) {
        return [Localizer getLocalizedText:@"No Comments"];
    }
    else if ([key isEqualToString:@"APPROVE_VIEW_ATTENDEES"])
    {
        return [Localizer getLocalizedText:@"No Attendees"];
    }
    else if ([key isEqualToString:@"APPROVE_INVOICES"])
    {
        return [Localizer getLocalizedText:@"MSG_NO_INVOICE_TO_APPROVE"];
    }
    
    return nil;
}

- (NSString*)lookUpImageName:(NSString*)key
{    
    if ([key isEqualToString:@"ACTIVE_ENTRIES"] || [key isEqualToString:@"REPORT_DETAIL"]) 
    {
        return @"neg_expense_icon";
    }
    else if ([key isEqualToString:@"AIRSHOPFILTER"] || [key isEqualToString:@"AIRSHOPSUMMARY"])
        return @"neg_airbooking_icon";
    else if ([key isEqualToString:@"OUT_OF_POCKET_LIST"])
    {
        return @"neg_quickexpense_icon";
    }
    else if ([key isEqualToString:@"INVOICE_RECEIPT"] || [key isEqualToString:@"RECEIPT_STORE_VIEWER"] || [key isEqualToString:@"RECEIPT_STORE_DETAIL_VIEW"] || [key isEqualToString:@"RECEIPT_DETAIL_VIEW"]) {
        return @"neg_receipt_icon";
    }
    else if ([key isEqualToString:@"ACTIVE_REPORTS"]) {
        return @"neg_report_icon";
    }
    else if ([key isEqualToString:@"TRIPS_LIST_NAVI"] || [key isEqualToString:@"TRIPS"]) {
        return @"neg_trips_icon";
    }
    else if ([key isEqualToString:@"ITEMIZATION_LIST"])
    {
        return @"neg_itemize_icon";
    }
    else if ([key isEqualToString:@"COMMENT_LIST"]) 
    {
        return @"neg_comments_icon";        
    }
    else if ([key isEqualToString:@"APPROVE_VIEW_ATTENDEES"])
    {
        return @"neg_attendee_icon";
    }
    else if ([key isEqualToString:UPLOAD_QUEUE_VIEW_CONTROLLER])
    {
        return @"neg_queue_icon";
    }
    else if ([delegate respondsToSelector:@selector(imageForNoDataView)])
    {
        return [delegate imageForNoDataView];
    }
    else
    {
        [iconImg setHidden:YES];
        return nil;
    }
}

- (NSString*)lookUpButtonTitle:(NSString*)key
{
    if (self.delegate != nil && [self.delegate canShowActionOnNoData]==NO)
    {
        [actionBtn setHidden:YES];
        return nil;
    }
    
    if ([key isEqualToString:@"ACTIVE_ENTRIES"] || [key isEqualToString:@"REPORT_DETAIL"]) 
    {
        return [Localizer getLocalizedText:@"Add Expense"];
    }
    else if ([key isEqualToString:@"ACTIVE_REPORTS"]) 
    {
        return [Localizer getLocalizedText:@"Create a Report"];
    }
    else if ([key isEqualToString:@"AIRSHOPSUMMARY"])
        return [Localizer getLocalizedText:@"Search Again"];
    else if ([key isEqualToString:@"FLIGHTSCHEDULE"])
        return [Localizer getLocalizedText:@"Return"];
    else if ([key isEqualToString:@"RECEIPT_STORE_VIEWER"] || [key isEqualToString:@"RECEIPT_DETAIL_VIEW"])
    {
        NSString *titleText = [Localizer getLocalizedText:@"Upload Receipt"]; // Receipt store => Upload Receipt. 
        
        if ([key isEqualToString:@"RECEIPT_STORE_VIEWER"] && [self.delegate isKindOfClass:[ReceiptStoreListView class]]) 
        {
            ReceiptStoreListView *listVC = (ReceiptStoreListView*)self.delegate;
            if (listVC.disableEditActions) 
            {
                [actionBtn setHidden:YES];
            }
        }
        else if ([key isEqualToString:@"RECEIPT_DETAIL_VIEW"] && [UIDevice isPad] && [self.delegate isKindOfClass:[ReportDetailViewController_iPad class]])
        {
            [actionBtn setHidden:YES];
        }
        else if ([key isEqualToString:@"RECEIPT_DETAIL_VIEW"])
        {
            ReceiptDetailViewController *receiptDetailVC = (ReceiptDetailViewController*)self.delegate;
            if (!receiptDetailVC.canUpdateReceipt) 
            {
                [actionBtn setHidden:YES];
            }
            
            titleText = [Localizer getLocalizedText:@"Attach Receipt"]; // All other cases => Attach Receipt
        }
        return titleText;
    }
//    else if ([key isEqualToString:@"TRIPS_LIST_NAVI"] || [key isEqualToString:@"TRIPS"]) 
//    {
//        return [Localizer getLocalizedText:@"Book Travel"];
//    }
    else if ([key isEqualToString:@"ITEMIZATION_LIST"])
    {
        return [Localizer getLocalizedText:@"Add Itemization"];
    }
    else if ([key isEqualToString:@"COMMENT_LIST"]) 
    {
        return [Localizer getLocalizedText:@"Add Comment"];
    }
    else if ([key isEqualToString:@"APPROVE_VIEW_ATTENDEES"])
    {
        if ([self.delegate isKindOfClass:[ReportAttendeesViewController class]])
        {
            ReportAttendeesViewController* vc = (ReportAttendeesViewController*) self.delegate;
            if (![vc canEdit])
                [actionBtn setHidden:YES];
        }
        return [Localizer getLocalizedText:@"ATTENDEE_ADD"]; // Add Attendee
    }
    else if ([delegate respondsToSelector:@selector(buttonTitleForNoDataView)])
    {
        return [delegate buttonTitleForNoDataView];
    }
    else
    {
        return nil;
    }   
}

@end

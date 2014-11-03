//
//  ReceiptDetailViewController.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/16/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReceiptDetailViewController.h"
#import "GetReceiptUrl.h"
#import "ReceiptStoreListView.h"
#import "ReceiptStoreReceipt.h"
#import "UploadReceiptData.h"

#define kAppendOrReplaceUploadAlert 84325 	//MOB-12019


@interface ReceiptDetailViewController (Private)
-(void)sendViewReceiptAuditMsg;
-(void) sendViewEntryReceiptAuditMsg;
- (void)showReceiptWebView;
- (void)updateReceiptSelected;
- (void)cameraSelected;
- (void)photoAlbumSelected;
- (void)receiptStoreSelected;
- (BOOL)showReceiptStoreAsSource;
- (void)deleteLocalReceipt;
- (void)adjustReceiptViewWithImage:(UIImage*)myimage;
@end

@implementation ReceiptDetailViewController
@synthesize isLoadingReceipt;
@synthesize canUpdateReceipt;
@synthesize receiptImg;
@synthesize receiptImgView;
@synthesize scrollView;
@synthesize isMimeTypePDF;
@synthesize pdfData;
@synthesize pdfWebView;
@synthesize delegate;
@synthesize isViewerOnlyMode;
@synthesize enableDeleteReceipt;
@synthesize receiptActionSheet;
@synthesize role;
@synthesize rpt;
@synthesize isApprovalReport;
@synthesize excludeReceiptStoreOption;
@synthesize isReportView;
@synthesize canShowActionSheet;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
	// MOB-10896: Added flag to check if its called from a Report
    return [self initWithNibName:nibNameOrNil bundle:nibBundleOrNil isReportView:NO];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil isReportView:(BOOL)isReportLevel
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        self.isReportView = isReportLevel;
    }
    return self;
}

- (NSString*)getViewIDKey
{
    return RECEIPT_DETAIL_VIEW;    
}

- (void)dealloc
{
/*  MOB-7967 This line below clears off the delegate of ExReceiptManager's instance. As a result, when it receives a response from server, the 
    ExReceiptManager does not know which view controller to forward it to. In case on MOB-7967, this resulted in the wait view not being dismissed
    and the application would have to be killed to get it to work again.
*/
//    [[ExReceiptManager sharedInstance] reset];
    
    
    pdfWebView.delegate = nil;
    
}

- (void)didReceiveMemoryWarning
{
    // Release any cached data, images, etc that aren't in use.
    self.receiptImg = nil;
    self.pdfData = nil;
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:@"INVOICE_IMAGE"]) 
    {
        self.isLoadingReceipt = NO;
        [self hideLoadingView];
        
        if (msg.errBody == nil && ([msg.errCode isEqualToString:@"200"] || msg.errCode == nil)) 
        {
            NSData *mydata = msg.data; 
            if (mydata != nil) 
            {
                self.receiptImg = [[UIImage alloc] initWithData:mydata];
                
                if (receiptImg == nil) {
                    [self showNoDataView:self];
                }
                else
                {
                    [[ExReceiptManager sharedInstance] cacheInvoiceReceipt:receiptImg];
                    
                    if ([self isViewLoaded]) 
                    {
                        if (receiptImg != nil) 
                        {
                            [self adjustReceiptViewWithImage:receiptImg];
                        }
                    }
                }//else
            }
        }
    }
	else if ([msg.idKey isEqualToString:@"OOPE_IMAGE"]) 
	{
        self.isLoadingReceipt = NO;
        [self hideLoadingView];
        
        if (msg.errBody == nil && ([msg.errCode isEqualToString:@"200"] || msg.errCode == nil)) 
        {
            if ([self isViewLoaded]) 
            {
                if ([receiptActionSheet isVisible] && enableDeleteReceipt) 
                {
                    [receiptActionSheet dismissWithClickedButtonIndex:(receiptActionSheet.numberOfButtons-1) animated:NO];
                    [self updateReceiptSelected];
                }
            }
            
            
            if ([msg.contentType isEqualToString:@"application/pdf"]) 
            {
                isMimeTypePDF = YES;
                self.pdfData = [NSMutableData dataWithData:msg.data];
                msg.data = nil;
                [[ExReceiptManager sharedInstance] cacheReportPDFReceipt:msg.data];
                [self showReceiptWebView];
            }
            else 
            {
                NSData *mydata = msg.data; 
                if (mydata != nil) 
                {
                    self.receiptImg = [[UIImage alloc] initWithData:mydata];
                    
                    if (receiptImg == nil) {
                        [self showNoDataView:self];
                    }
                    else
                    {
                        if ([self showReceiptStoreAsSource] && [[ExReceiptManager sharedInstance] getReceiptUploadType] != 2) {
                            [[ExReceiptManager sharedInstance] updateReportEntryImage:receiptImg];
                        }
                        else {
                            
                            [[ExReceiptManager sharedInstance] updateMobileEntryImage:receiptImg];
                        }
                        
                        
                        if ([self isViewLoaded]) 
                        {
                            if (receiptImg != nil) 
                            {
                                [self adjustReceiptViewWithImage:receiptImg];
                            }
                        }
                    }//else
                }//if
                
            }
            
            // MOB-6132
            // MOB-10146 check for PDF as well as image files as entry receipt
            if (isApprovalReport && (pdfData != nil || receiptImg != nil) && [[ExReceiptManager sharedInstance] getReceiptUploadType] == 1) {
                // Audit approver has viewed image.
                [self sendViewEntryReceiptAuditMsg];  
            }
            else if (isApprovalReport && (pdfData != nil || receiptImg != nil)) {
                // Audit approver has viewed image.
                [self sendViewReceiptAuditMsg];  
            }
        }
    }
    else if ([msg.idKey isEqualToString:GET_RECEIPT_URL]) 
	{
		if ([self isViewLoaded]) 
		{
			GetReceiptUrl* rUrl = (GetReceiptUrl*)msg.responder;
			if (msg.errBody != nil || ![rUrl.status isEqualToString:@"SUCCESS"]) 
			{
				NSString* errMsg = msg.errBody != nil ? msg.errBody : nil;
				
				if(errMsg == nil)
					errMsg = [Localizer getLocalizedText:@"Cannot access receipt"];
				
				UIAlertView *alert = [[MobileAlertView alloc] 
									  initWithTitle:msg.errCode
									  message:errMsg
									  delegate:nil 
									  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
									  otherButtonTitles:nil];
				[alert show];
			}
			else
			{
				if (rUrl != nil && rUrl.receiptUrl != nil) 
				{
					[RequestController retrieveImageFromUrl:rUrl.receiptUrl MsgId:@"OOPE_IMAGE" SessionID:[ExSystem sharedInstance].sessionID MVC:self];	
				}	
			}
		}
	}
    else if ([msg.idKey isEqualToString:SAVE_REPORT_ENTRY_RECEIPT])
    {
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
        
        SaveReportEntryReceipt *saveReportEntryResp = (SaveReportEntryReceipt*)msg.responder;
        
        if (msg.errBody == nil && [saveReportEntryResp.actionStatus.status isEqualToString:@"SUCCESS"]) 
        {
            if (self.delegate != nil && [delegate respondsToSelector:@selector(savedReportEntryReceipt:)]) {
                [delegate savedReportEntryReceipt:saveReportEntryResp];
            }
        
            if ([self isViewLoaded]) {
                [self.navigationController popViewControllerAnimated:YES];
            }
        }
        else 
        {
            NSString *errMsg = msg.errBody;
            
            if (errMsg == nil && [saveReportEntryResp.actionStatus.errMsg length])
                errMsg = saveReportEntryResp.actionStatus.errMsg;
            
            if (errMsg ==  nil)
                errMsg = [Localizer getLocalizedText:@"Check connection retry from receipt store"];
            
            UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Save entry receipt failed"] 
                                                                message:errMsg 
                                                               delegate:nil 
                                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                                      otherButtonTitles:
                                  nil];
            [alert show];
            
            // Revert to NoDataView or old image
            self.receiptImg = self.receiptImgView.image;
            if (self.receiptImg == nil)
                [self showNoDataView:self];
        }
    }
    else if ([msg.idKey isEqualToString:SAVE_REPORT_RECEIPT2])
    {
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
    
        if (self.delegate != nil && [delegate respondsToSelector:@selector(savedReportReceipt:)]) {
            [delegate savedReportReceipt];
        }
        
        if ([self isViewLoaded]) {
            [self.navigationController popViewControllerAnimated:YES];
        }
    }
    else if ([msg.idKey isEqualToString:@"UPLOAD_IMAGE_DATA"])
    {
        // This block will be hit only if it's a Mobile entry
        // This block will also be hit when there is a receipt upload failure
        // MOB-9414 need to handle failure here
        if ([self isViewLoaded])
        {
            [self hideWaitView];
        }
        UploadReceiptData *receiptData = (UploadReceiptData*)msg.responder;
        // MOB-9414 check for error status & revert back to previous state
        if (msg.errBody != nil || msg.responseCode >= 400 || ![receiptData.returnStatus isEqualToString:@"SUCCESS"]) 
        {
            // MOB-9653 handle imaging not configured error
            if (![ExReceiptManager handleImageConfigError:msg.errBody])
            {
                NSString *errMsg = msg.errBody != nil? msg.errBody : 
                [Localizer getLocalizedText:@"ReceiptUploadFailMsg"];
                
                UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"] 
                                                                    message:errMsg 
                                                                   delegate:nil 
                                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                                          otherButtonTitles:
                                      nil];
                [alert show];
            }
            // Revert to NoDataView or old image
            self.receiptImg = self.receiptImgView.image;
            if (self.receiptImg == nil)
                [self showNoDataView:self];

        }
        else 
        {
            // Return, if success
            if ([self isViewLoaded])
            {
                [self.navigationController popViewControllerAnimated:YES];
            }
        }
    }
    //MOB-12019
    else if ([msg.idKey isEqualToString:APPEND_RECEIPT])
    {
        [self hideWaitView];
        if (self.delegate != nil && [delegate respondsToSelector:@selector(updatedReportEntryReceipt:)]) {
            [delegate updatedReportEntryReceipt: msg];
        }
        [self.navigationController popViewControllerAnimated:YES];
    }

}

#pragma mark - View lifecycle
-(void) dismissView
{
    [self.view removeAllSubviews];
    [self.navigationController dismissViewControllerAnimated:YES completion:nil];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.navigationController setToolbarHidden:NO];
    canShowActionSheet = NO;
    
    if (receiptImg != nil) 
    {
        [self adjustReceiptViewWithImage:receiptImg];
    }
    else if(pdfData != nil)
    {
        [self showReceiptWebView];
    }
    else if (receiptImg == nil && !isLoadingReceipt && pdfData == nil) 
    {
        [self showNoDataView:self];
        // MOB-11696 : show action sheet only if its quickexpense.
        if(!self.isReportView && [ExSystem connectedToNetwork] )
            canShowActionSheet = YES;
    }
    else
    {
        if ([ExSystem connectedToNetwork])
            [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Receipt Image(s)"]];
        else
        {
            // MOB-10973 Show offline view and offline bar
            [self showOfflineView:self];
            [self makeOfflineBar];
        }
    }
    
    if (isViewerOnlyMode) {
        canUpdateReceipt = NO;   
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStylePlain target:self action:@selector(dismissView)];
        if([UIDevice isPad])
            [self.navigationItem setRightBarButtonItem:btnClose animated:NO];
        else
            self.navigationItem.rightBarButtonItem = nil;
    }
    
    if (canUpdateReceipt) {
        
        UIBarButtonItem *btnAdd = nil;
        
        if (receiptImg == nil && !isLoadingReceipt)
        {
            btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(updateReceiptSelected)];
		}
        else
        {
            NSString *btnTitle = [Localizer getLocalizedText:@"Update"];  
            btnAdd = [[UIBarButtonItem alloc] initWithTitle:btnTitle style:UIBarButtonItemStylePlain target:self action:@selector(updateReceiptSelected)];
        }
        self.navigationItem.rightBarButtonItem = nil;
        [self.navigationItem setRightBarButtonItem:btnAdd animated:NO];
    }
}


- (void)viewDidAppear:(BOOL)animated
{
	
    [super viewDidAppear:animated];
    // MOB-11696 : show action sheet only if its quickexpense.
    if(canShowActionSheet)
    {
        [self updateReceiptSelected];
    }
}


- (void)viewDidUnload
{
    [super viewDidUnload];
    self.receiptImg = nil;
    self.scrollView = nil;
    self.receiptImgView = nil;
    //self.delegate = nil; //MOB-10144 don't clear the delegate or there won't be anyone to notify when the image is selected
    self.pdfWebView = nil;
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
	[self adjustReceiptViewWithImage:receiptImg];
}

#pragma mark Scaled view
-(void)adjustReceiptViewWithImage:(UIImage*)myimage
{
	if (myimage != nil)
	{
		int w = myimage.size.width;
		int h = myimage.size.height;
    
		float screenW = self.view.frame.size.width;
		float screenH = self.view.frame.size.height;
		
        receiptImgView.contentMode = UIViewContentModeScaleAspectFit;
        receiptImgView.multipleTouchEnabled = YES;
        receiptImgView.backgroundColor = [UIColor clearColor];
        
		receiptImgView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
		scrollView.frame = CGRectMake(0, 0, screenW, screenH);
		
		scrollView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
		receiptImgView.image = myimage;
		
		scrollView.contentSize = CGSizeMake(w, h);
		scrollView.maximumZoomScale = 8.0;
		//MOB-11244
		scrollView.minimumZoomScale = 1.0;
		scrollView.clipsToBounds = YES;
		scrollView.delegate = self;
        scrollView.backgroundColor = [UIColor clearColor];
        // MOB-11242
        // add gesture recognizers to the image view
        receiptImgView.userInteractionEnabled = YES;
        scrollView.userInteractionEnabled = YES;
        UITapGestureRecognizer *doubleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleDoubleTap:)];
        [doubleTap setNumberOfTapsRequired:2];
        [receiptImgView addGestureRecognizer:doubleTap];
 
	}
}

#pragma mark TapDetectingImageViewDelegate methods

- (void)handleDoubleTap:(UIGestureRecognizer *)gestureRecognizer {
    // if zoom scale is max then reset the image scale.
    float newScale;
    if([scrollView zoomScale] != 1.0 )
    {
        // double tap resets zooms
        newScale = 1.0 ;
    }
    else
        newScale = [scrollView zoomScale] * 1.5;
    CGRect zoomRect = [self zoomRectForScale:newScale withCenter:[gestureRecognizer locationInView:gestureRecognizer.view]];
    [scrollView zoomToRect:zoomRect animated:YES];
}


#pragma mark Utility methods

- (CGRect)zoomRectForScale:(float)scale withCenter:(CGPoint)center {
    CGRect zoomRect;
    
    zoomRect.size.height = [scrollView frame].size.height / scale;
    zoomRect.size.width  = [scrollView frame].size.width  / scale;
    
    zoomRect.origin.x    = center.x - (zoomRect.size.width  / 2.0);
    zoomRect.origin.y    = center.y - (zoomRect.size.height / 2.0);
    
    return zoomRect;
}

#pragma mark Receipt Actions
- (void)showReceiptWebView 
{ 
    if (pdfWebView == nil) 
    {
        [scrollView setScrollEnabled:NO];
        [scrollView removeFromSuperview];
        self.pdfWebView = [[UIWebView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    }
    
    [pdfWebView loadData:pdfData MIMEType:@"application/pdf" textEncodingName:@"UTF-8" baseURL:nil];
    [pdfWebView setScalesPageToFit:YES];
    [self.view addSubview:pdfWebView];
}

- (BOOL) showReceiptStoreAsSource
{
    if ([[ExReceiptManager sharedInstance] getReceiptUploadType] != 3)
    {
        return YES;
    }
    else
    {
        return NO;
    }
}

-(BOOL) shouldHideReceiptStore
{
    bool hideReceiptStore = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"HIDE_RECEIPT_STORE" withType:@"CTE_EXPENSE_ADMIN"]];
    return (hideReceiptStore || self.excludeReceiptStoreOption);
}

-(void)updateReceiptSelected
{
    NSString *receiptStoreOption = nil;
    NSString *deleteOption = nil; // Used for delayed receipt saving
    
    if (self.receiptActionSheet != nil) 
    {
        self.receiptActionSheet = nil;
    }
    
    bool hideReceiptStore = [self shouldHideReceiptStore];

    if ([self showReceiptStoreAsSource] && !hideReceiptStore) 
    {
        receiptStoreOption = [Localizer getLocalizedText:@"Receipt Store"];  
    }

    if (enableDeleteReceipt && !isLoadingReceipt) 
    {
       deleteOption = [Localizer getLocalizedText:@"Delete Receipt"]; 
    }
            
    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
	{
        if (receiptStoreOption != nil)
        {
            self.receiptActionSheet = [[MobileActionSheet alloc] initWithTitle:nil 
                                                             delegate:self 
                                                    cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:[Localizer getLocalizedText:@"Camera"],
                                  [Localizer getLocalizedText:@"Photo Album"],
                                  receiptStoreOption, 
                                  deleteOption,
                                  nil];
        }
        else
        {
            self.receiptActionSheet = [[MobileActionSheet alloc] initWithTitle:nil 
                                                             delegate:self 
                                                    cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:[Localizer getLocalizedText:@"Camera"],
                                  [Localizer getLocalizedText:@"Photo Album"],
                                  deleteOption,
                                  nil];
        }
    }
    else
    {
        if (receiptStoreOption != nil)
        {
            self.receiptActionSheet = [[MobileActionSheet alloc] initWithTitle:nil 
                                                             delegate:self 
                                                    cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:[Localizer getLocalizedText:@"Photo Album"],
                                  receiptStoreOption, 
                                  deleteOption,
                                  nil];
        }
        else
        {
            self.receiptActionSheet = [[MobileActionSheet alloc] initWithTitle:nil 
                                                             delegate:self 
                                                    cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:[Localizer getLocalizedText:@"Photo Album"],
                                  deleteOption,
                                  nil];
        }
    }
    
    if([UIDevice isPad])
	{
        CGRect rect = self.navigationController.navigationBar.frame;
		[receiptActionSheet showFromRect:rect inView:self.view animated:YES];
	}
	else 
    {
        receiptActionSheet.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
        [receiptActionSheet showFromToolbar:self.navigationController.toolbar];
    }

}

- (void)cameraSelected
{    
    if([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera])
    {
        NSString *addedTo = self.isReportView ? @"Report Entry" : @"Mobile Entry" ;
        NSDictionary *dict = @{@"Added Using": @"Camera", @"Added To": addedTo};
        [Flurry logEvent:@"Receipts: Add" withParameters:dict];
        UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
        imgPicker.sourceType = UIImagePickerControllerSourceTypeCamera;
//        imgPicker.allowsEditing = NO;
        [UnifiedImagePicker sharedInstance].delegate = self;
        
        if ([UIDevice isPad]) 
        {
            if(pickerPopOver != nil)
            {
                [pickerPopOver dismissPopoverAnimated:YES];
            }
            
            self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];
            CGRect rect = CGRectMake(self.navigationController.navigationBar.frame.size.width - self.navigationController.navigationItem.rightBarButtonItem.width, 0, 1, 1);
            [pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
        }
        else
        {
            [self presentViewController:imgPicker animated:YES completion:nil];
        }
    }
}

- (void)photoAlbumSelected
{
    if([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypePhotoLibrary])
    {
        NSString *addedTo = self.isReportView ? @"Report Entry" : @"Mobile Entry" ;
        NSDictionary *dict = @{@"Added Using": @"Album", @"Added To": addedTo};
        [Flurry logEvent:@"Receipts: Add" withParameters:dict];
        UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker];
        imgPicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        [UnifiedImagePicker sharedInstance].delegate = self;
        imgPicker.allowsEditing = YES;

        if ([UIDevice isPad]) 
        {
            if(pickerPopOver != nil)
            {
                [pickerPopOver dismissPopoverAnimated:YES];
            }
            
            self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];
            CGRect rect = CGRectMake(self.navigationController.navigationBar.frame.size.width - self.navigationController.navigationItem.rightBarButtonItem.width, 0, 1, 1);
            [pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
        }
        else
        {
            [self presentViewController:imgPicker animated:YES completion:nil];
        }
    }
}

-(void)deleteLocalReceipt
{
    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Confirm Delete"] 
                                                        message:[Localizer getLocalizedText:@"CONFIRM_RECEIPT_DELETION"] 
                                                       delegate:self 
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
                                              otherButtonTitles:[Localizer getLocalizedText:@"OK"],
                                                                nil];
    [alert show];
}

- (void)receiptStoreSelected
{
    ReceiptStoreListView *receiptStoreView = [[ReceiptStoreListView alloc] initWithNibName:@"ReceiptStoreListView" bundle:nil];
	receiptStoreView.delegate = self;
    receiptStoreView.disableEditActions = YES;
    
	UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:receiptStoreView];
    if ([UIDevice isPad])
    {
        navController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
        navController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    }
    else
    {
        navController.toolbar.tintColor = [UIColor darkBlueConcur_iOS6];
        navController.navigationBar.tintColor = [UIColor darkBlueConcur_iOS6];        
    }
	navController.modalPresentationStyle = UIModalPresentationFormSheet;
	[self presentViewController:navController animated:YES completion:nil];
}

-(void) sendViewReceiptAuditMsg
{
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/MarkReportReceiptsAsViewed/%@", [ExSystem sharedInstance].entitySettings.uri, self.rpt.rptKey];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	RequestController *rc = [RequestController alloc];	
	Msg *msg = [[Msg alloc] initWithData:@"MarkReportReceiptsAsViewed" State:@"" Position:nil MessageData:nil URI:path MessageResponder:nil ParameterBag:pBag];
	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	msg.skipCache = YES;
	
	[rc initDirect:msg MVC:self];				
}


-(void) sendViewEntryReceiptAuditMsg
{
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/MarkEntryReceiptAsViewed/%@", [ExSystem sharedInstance].entitySettings.uri, [ExReceiptManager sharedInstance].data.reportEntry.rpeKey];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	RequestController *rc = [RequestController alloc];	
	Msg *msg = [[Msg alloc] initWithData:@"MarkEntryReceiptAsViewed" State:@"" Position:nil MessageData:nil URI:path MessageResponder:nil ParameterBag:pBag];
	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	msg.skipCache = YES;
	
	[rc initDirect:msg MVC:self];				
}


#pragma mark UIAlertView Delegate methods
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex 
{
    if (buttonIndex == 1)
    {
        if (delegate != nil && [delegate respondsToSelector:@selector(selectedImageForNewEntry:)]) 
        {
            [delegate selectedImageForNewEntry:nil];
        }
        [self.navigationController popViewControllerAnimated:YES];
    }    
}

#pragma mark UIActionSheet Delegate methods
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex 
{	
	BOOL hasCamera = [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera];
	int offset = hasCamera? 1 : 0;
	
	if (hasCamera && (buttonIndex ==(-1 + offset)))
	{
		[self cameraSelected];
	} 
	else if (buttonIndex == (0 + offset))
	{
		[self photoAlbumSelected];
	} 
	else if (buttonIndex == (1 + offset))
	{
        bool hideReceiptStore = [self shouldHideReceiptStore];
        if ([self showReceiptStoreAsSource] && !hideReceiptStore) {
            [self receiptStoreSelected];
        }
        else if (enableDeleteReceipt)
        {
            [self deleteLocalReceipt];
        }
        else
        {
            [actionSheet dismissWithClickedButtonIndex:buttonIndex animated:YES];
        }
	}
    else if (buttonIndex == (2 + offset))
    {
        if (enableDeleteReceipt && [self showReceiptStoreAsSource] && ![self shouldHideReceiptStore])
        {
            [self deleteLocalReceipt];
        }
        else
        {
            [actionSheet dismissWithClickedButtonIndex:buttonIndex animated:YES];
        }
    }
}

#pragma mark NoDataViewDelegate method
- (void)actionOnNoData:(id)sender
{
    [self updateReceiptSelected];
}

- (NSString *)instructionForNoDataView
{
    return nil;
}

#pragma mark UIScrollView delegate methods
- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    if (!isMimeTypePDF) 
    {
        return receiptImgView;
    }
    else 
    {
        return pdfWebView;
    }
}

#pragma mark UnifiedImagePickerDelegate methods
- (void)unifiedImagePickerSelectedImage:(UIImage*)image
{
	if ([UIDevice isPad])
	{
		if (self.pickerPopOver != nil)
		{
			[self.pickerPopOver dismissPopoverAnimated:YES];
			self.pickerPopOver = nil;
		}
        // MOB-8441 Do not dismiss the image picker modally - it is always presented within popover for iPad
	}
	else {
		[[[UnifiedImagePicker sharedInstance] imagePicker] dismissViewControllerAnimated:YES completion:nil];
	}
    
  	//MOB-12019
    if (![ExReceiptManager sharedInstance].bypass && [ExReceiptManager sharedInstance].data.reportEntry.receiptImageId != nil)
    {
        [self launchAppendAlertWithData:image];
        return;
    }
    
    
    
	self.receiptImg = image;
    
    if (![ExReceiptManager sharedInstance].bypass)
    {        
        [self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
        [[ExReceiptManager sharedInstance] uploadReceipt:receiptImg];
    }
    else
    {
        if (self.delegate != nil && [self.delegate respondsToSelector:@selector(selectedImageForNewEntry:)]) 
        {
            [self.delegate selectedImageForNewEntry:self.receiptImg];
        }
        
        [self.navigationController popViewControllerAnimated:YES];
    }
}

#pragma mark - UIAlertViewDelegate methods
//MOB-12019
-(void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    MobileAlertView* mobileAlertView = (MobileAlertView*)alertView;

    if (alertView.tag == kAppendOrReplaceUploadAlert)
    {
        if (buttonIndex ==0)
            return;
        
        NSString* idOfReceiptOnWhichToAppendOrNil = buttonIndex == 1? [ExReceiptManager sharedInstance].data.reportEntry.receiptImageId : nil;
        
        NSObject *newItem = mobileAlertView.eventData;
        
        NSString* idOfAppendingReceipt = nil;
        UIImage* appendingReceipt = nil;
        
        
        if ([newItem isKindOfClass:[NSString class]])
            idOfAppendingReceipt = (NSString*)newItem;
        
        if ([newItem isKindOfClass:[UIImage class]])
            appendingReceipt = (UIImage*)newItem;
        

        if (idOfAppendingReceipt != nil)
        {
            [self showWaitView];
            [[ExReceiptManager sharedInstance] saveReportEntryWithNewReceipt:idOfAppendingReceipt appendToReceiptId:idOfReceiptOnWhichToAppendOrNil];
        }
        else if (appendingReceipt != nil)
        {
            [self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
            [[ExReceiptManager sharedInstance] uploadReceipt:appendingReceipt appendToReceiptId:idOfReceiptOnWhichToAppendOrNil];
        }  
    }
}

-(void) launchAppendAlertWithData:(NSObject*)id
{
    MobileAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:@"Append Confirmation"
                              message:@"Append or Replace Receipt"
                              delegate:self
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                              otherButtonTitles:[Localizer getLocalizedText:@"Append"], [Localizer getLocalizedText:@"Replace"], nil];
    alert.tag = kAppendOrReplaceUploadAlert;
    alert.eventData = id;
    [alert show];
}

#pragma mark ReceiptStoreViewDelegate method
-(void)didSelectImageFromReceiptStore:(ReceiptStoreReceipt*)receiptData
{
	[self dismissViewControllerAnimated:YES completion:nil];
    NSString *addedTo = self.isReportView ? @"Report Entry" : @"Mobile Entry" ;
    NSDictionary *dict = @{@"Added Using": @"Receipt Store", @"Added To": addedTo};
    [Flurry logEvent:@"Receipts: Add" withParameters:dict];
	//MOB-12019
    if (![ExReceiptManager sharedInstance].bypass && [ExReceiptManager sharedInstance].data.reportEntry.receiptImageId != nil)
    {
        [self launchAppendAlertWithData:receiptData.receiptImageId];
        return;
    }
    
    if (![ExReceiptManager sharedInstance].bypass)
    {
        [self showWaitView];

        // MOB-9414 Do not cache receipt image id before save succeeds.
        [ExReceiptManager sharedInstance].isReceiptStore = YES;
        [ExReceiptManager sharedInstance].delegate = self;

        if ([ExReceiptManager sharedInstance].currentReceiptUploadType == 0) 
        {
            [ExReceiptManager sharedInstance].data.reportData.receiptImageId = receiptData.receiptImageId;            
            [[ExReceiptManager sharedInstance] continueSaveReceipt];
        } 
        else if ([ExReceiptManager sharedInstance].currentReceiptUploadType == 1)
        {
//            [ExReceiptManager sharedInstance].data.reportEntry.receiptImageId = receiptData.receiptImageId; 
            [[ExReceiptManager sharedInstance] saveReportEntryWithNewReceipt:receiptData.receiptImageId appendToReceiptId:nil];
        }
    }
    else
    {        
        if (self.delegate != nil && [self.delegate respondsToSelector:@selector(selectedReceiptStoreImageForNewEntry:)]) 
        {
            [self.delegate selectedReceiptStoreImageForNewEntry:receiptData.receiptImageId];
        }
        
        [self.navigationController popViewControllerAnimated:YES];
    }
}

#pragma mark ExReceiptManagerDelegate method
-(void) handleReceiptFailure
{
    // MOB-9416 Dimiss wait view, etc.
    if ([self isViewLoaded]) {
        [self hideWaitView];
    }
    
    // Revert to NoDataView or old image
    self.receiptImg = self.receiptImgView.image;
    if (self.receiptImg == nil)
        [self showNoDataView:self];
}

#pragma mark - MobileViewController overrides
-(BOOL) canShowOfflineTitleForNoDataView
{
    return NO;
}

// MOB-10896 : Override to distinguish between report level and reciept level
-(NSString*) titleForNoDataView
{
    if(self.isReportView)
    {
        return [Localizer getLocalizedText:@"NO_REPORTRECEIPT_NEG"];
    }
    else
    {
       return [Localizer getLocalizedText:@"NO_RECEIPT_NEG"];
    }
}

@end

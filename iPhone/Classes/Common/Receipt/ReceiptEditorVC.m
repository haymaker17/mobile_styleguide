//
//  ReceiptEditorVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "ReceiptEditorVC.h"
#import "UnifiedImagePicker.h"
#import "ReceiptUploader.h"
#import "ReceiptDownloader.h"
#import "ReceiptCache.h"
#import "GetReceiptUrl.h"
#import "UploadableReceipt.h"
#import "AppendReceiptData.h"
#import "ReceiptStoreListView.h"
#import "PostMsgInfo.h"
#import "UploadQueueItemManager.h"
#import "UploadQueue.h"
#import "Config.h"
#import "CCImagePickerViewController.h"
#import "AnalyticsTracker.h"

#define kAlertViewDeleteReceipt	101326
#define kAlertViewEReceiptWithoutReceipt 10327

@interface ReceiptEditorVC ()
{
    BOOL isCameraSelected;
}
@property (strong, nonatomic) CCImagePickerViewController *picker;
@property (strong, nonatomic) UIAlertController *alertController;
- (void)showReceiptWebView;
- (void)updateReceiptSelected;
- (void)cameraSelected;
- (void)photoAlbumSelected;
- (void)receiptStoreSelected;
- (void)deleteLocalReceipt;
- (void)adjustReceiptViewWithImage:(UIImage*)myimage;
- (void)loadReceiptFromCacheByReceiptId;
- (void)retrieveReceiptUrl;
- (void)downloadReceipt;
- (void)queueReceipt:(UIImage*)receiptImage;
- (void)receiptQueued;
- (void)closeMe;

@end

@implementation ReceiptEditorVC {
}

SEL actionShetSelector = nil;

- (NSString*)getViewIDKey
{
    return RECEIPT_DETAILS;
}

-(void)setSeedData:(Receipt*) rcpt
{
    self.receipt = rcpt;
    if (self.receipt == nil)
        self.receipt = [[Receipt alloc] init];
    else
    {
        BOOL isNullData = NO;
        if ([self.receipt isPDF] && self.receipt.pdfData == nil)
            isNullData = YES;
        else if (![self.receipt isPDF] && self.receipt.receiptImg == nil)
            isNullData = YES;
        if (isNullData )
        {
            if ([[self.receipt fileCacheKey] length])
            {
                // MOB-13593 - Force reload the receipts if its from report header. 
                if(!self.ignoreCache)
                    [self loadReceiptFromCacheByReceiptId];
                
                if (![self.receipt isLoaded])
                {	
                    if (![self.receipt.url length])
                    {
                        [self retrieveReceiptUrl];
                    }
                    else
                        [self downloadReceipt];
                }
            }
            else if ([self.receipt.localReceiptId length])
            {
                // We have a local receipt, i.e. one that has not yet been uploaded to the server
                NSString *filePath = [UploadableReceipt filePathForLocalReceiptImageId:self.receipt.localReceiptId isPdfReceipt:[self.receipt isPDF]];
                if (filePath != nil && filePath.length > 0)
                {
                    // We found the file in which the local receipt image is stored
                    self.receipt.receiptImg = [UIImage imageWithContentsOfFile:filePath];
                }
            }
            else if ([self.receipt.url length]) // Report receipt, no receipt id, has url
            {
                if (![self.receipt isLoaded])
                {
                    [self downloadReceipt];
                }
            }
        }
    }
}

- (void)dealloc
{
    self.pdfWebView.delegate = nil;
}

- (void)didReceiveMemoryWarning
{
    // Release any cached data, images, etc that aren't in use.
    //    self.receiptImg = nil;
    //    self.pdfData = nil;
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
}


#pragma mark - View lifecycle
//-(void) dismissView
//{
//	for(UIView *v in self.view.subviews)
//		[v removeFromSuperview];
//    [self.navigationController dismissModalViewControllerAnimated:YES];
//}

-(void) setupToolbar
{
    //self.showActionSheet = NO;
    [self setShowActionSheet:NO];
    if(([ExSystem connectedToNetwork] || self.supportsOffline) && (self.canUpdate || self.canAppend))
	{
        NSMutableArray* buttons = [[NSMutableArray alloc] initWithCapacity:3];
        
        if (self.canDelete && [self.receipt hasReceipt])
        {
            UIBarButtonItem *btnDelete = nil;
            NSString* title = [@"Detach" localize];
            NSString *selString = @"deleteLocalReceipt";
            
#ifdef CORP
            btnDelete = [ExSystem makeSilverBarButton:title width:100.0 height:32.0 selectorString:selString target:self];
#else
            btnDelete = [[UIBarButtonItem alloc] initWithTitle:title style:UIBarButtonItemStyleBordered target:self action:NSSelectorFromString(selString)];
#endif
            [buttons addObject:btnDelete];
        }
        
        if ((self.canUpdate &&( self.receipt== nil || ![self.receipt hasReceipt])) || self.canAppend)
        {
            UIBarButtonItem *btnAdd = nil;
            NSString* title = [@"Add Receipt" localize];
            NSString *selString = @"updateReceiptSelected";
            if ([self.receipt hasReceipt])
            {
                title = [@"Add Another" localize];
                if ([self.receipt.receiptId length])
                    selString = @"appendReceiptSelected";
                else
                {
                    // Report receipt, no id, no need to call append, just notify the update event to delegate
                    selString = @"updateReceiptSelected";
                }
            }
            // MOB-13792 - donot show action sheet when receipt is present in report header.
            else {
                self.showActionSheet = YES;
            }
#ifdef CORP
            btnAdd = [ExSystem makeSilverBarButton:title width:100.0 height:32.0 selectorString:selString target:self];
#else
            btnAdd = [[UIBarButtonItem alloc] initWithTitle:title style:UIBarButtonItemStyleBordered target:self action:NSSelectorFromString(selString)];
#endif
			// Set flag so we can show the action sheet later.
			if([self respondsToSelector:NSSelectorFromString(selString)])
            {
                
                actionShetSelector = NSSelectorFromString(selString);
                
            }
            [buttons addObject:btnAdd];
        }
        
        if (self.canUpdate && [self.receipt hasReceipt])
        {
            NSString* title = [@"Replace" localize];
            NSString *selString = @"updateReceiptSelected";
            UIBarButtonItem *btnReplace = nil;
            //            UIBarButtonItem *btnReplace = [[UIBarButtonItem alloc] initWithTitle:[@"Replace" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(updateReceiptSelected)];
            //            btnReplace.tintColor = [UIColor colorWithRed:220.0/255 green:220.0/255 blue:220.0/255 alpha:1.0];
#ifdef CORP
            btnReplace = [ExSystem makeSilverBarButton:title width:100.0 height:32.0 selectorString:selString target:self];
#else
            btnReplace = [[UIBarButtonItem alloc] initWithTitle:title style:UIBarButtonItemStyleBordered target:self action:NSSelectorFromString(selString)];
#endif
            [buttons addObject:btnReplace];
        }
        
        if ([buttons count] >0)
        {
            UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
            
            if ([buttons count]>2)
                [buttons insertObject:flexibleSpace atIndex:2];
            if ([buttons count]>1)
                [buttons insertObject:flexibleSpace atIndex:1];
            [buttons insertObject:flexibleSpace atIndex:0];
            [buttons addObject:flexibleSpace];
            [self.tbar setItems:buttons animated:YES];
        }
    }
    else
    {
        self.navigationItem.rightBarButtonItem = nil;
    }
    
    if([UIDevice isPad] && [self.navigationController.viewControllers count]<=1)
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStylePlain target:self action:@selector(closeMe)];
        [self.navigationItem setLeftBarButtonItem:btnClose animated:NO];
    }
    

}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // HACK!!! This screen wants to slide under the navigation bar but most of our viewControllers do not
    // MOB-16959
    // MOB-21215 We don't need to stretch to top.
    if ([self respondsToSelector:@selector(edgesForExtendedLayout)])
    {
        self.edgesForExtendedLayout = UIRectEdgeLeft | UIRectEdgeBottom | UIRectEdgeRight;
    }

#ifdef CORP
    [self.tbar setBackgroundImage:[UIImage imageNamed:@"bottom_bar_silver"] forToolbarPosition:UIToolbarPositionAny barMetrics:UIBarMetricsDefault];
#endif
    
    self.title = [Localizer getLocalizedText:@"RECEIPT_VIEWER"];
    
    if (self.receipt.receiptImg != nil)
    {
        [self adjustReceiptViewWithImage:self.receipt.receiptImg];
    }
    else if([self.receipt isPDF] && self.receipt.pdfData != nil)
    {
        [self showReceiptWebView];
    }
    else if (![self.receipt hasReceipt])
    {
        [self showNoDataView:self];
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
    
    [self setupToolbar];
}


- (void)viewWillAppear:(BOOL)animated
{
    ConcurMobileAppDelegate *appDelegate = [[UIApplication sharedApplication] delegate];
    if ( (self.receipt.receiptImg == nil) && (appDelegate != nil) && (appDelegate.isUploadPdfReceipt) && [self.receipt isPDF] && self.receipt.pdfData != nil )
    {
        [self showReceiptWebView];
    }
    else
    	[super viewWillAppear:animated];
    // Counter home page's tool bar background image setting
    [self.navigationController setToolbarHidden:YES];
    
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    if (self.showActionSheet) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self updateReceiptSelected];
        });
        self.showActionSheet = NO;
    }
}

#pragma mark Scaled view
-(void)adjustReceiptViewWithImage:(UIImage*)myimage
{
	if (myimage != nil)
	{
        //		int w = myimage.size.width;
        //		int h = myimage.size.height;
        
		float screenW = self.view.frame.size.width;
		float screenH = self.view.frame.size.height - self.tbar.frame.size.height;
		
        self.receiptImgView.contentMode = UIViewContentModeScaleAspectFit;
        self.receiptImgView.multipleTouchEnabled = YES;
        self.receiptImgView.backgroundColor = [UIColor clearColor];
        
		self.receiptImgView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
		self.scrollView.frame = CGRectMake(0, 0, screenW, screenH);
		
		self.scrollView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
		self.receiptImgView.image = myimage;
		
        // Scrollview content size should be the receiptImgView size.  Make sure the picture fill the screen.
        //		scrollView.contentSize = CGSizeMake(w, h);
		self.scrollView.maximumZoomScale = 8.0;
		//MOB-11244
		self.scrollView.minimumZoomScale = 1.0;
		self.scrollView.clipsToBounds = YES;
		self.scrollView.delegate = self;
        self.scrollView.backgroundColor = [UIColor clearColor];
        // MOB-11242
        // add gesture recognizers to the image view
        self.receiptImgView.userInteractionEnabled = YES;
        self.scrollView.userInteractionEnabled = YES;
        UITapGestureRecognizer *doubleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleDoubleTap:)];
        [doubleTap setNumberOfTapsRequired:2];
        [self.receiptImgView addGestureRecognizer:doubleTap];
        
	}
}

- (void)handleDoubleTap:(UIGestureRecognizer *)gestureRecognizer {
    // if zoom scale is max then reset the image scale.
    float newScale;
    if([self.scrollView zoomScale] != 1.0 )
    {
        // double tap resets zooms
        newScale = 1.0 ;
    }
    else
        newScale = [self.scrollView zoomScale] * 1.5;
    CGRect zoomRect = [self zoomRectForScale:newScale withCenter:[gestureRecognizer locationInView:gestureRecognizer.view]];
    [self.scrollView zoomToRect:zoomRect animated:YES];
}

- (CGRect)zoomRectForScale:(float)scale withCenter:(CGPoint)center {
    CGRect zoomRect;
    
    zoomRect.size.height = [self.scrollView frame].size.height / scale;
    zoomRect.size.width  = [self.scrollView frame].size.width  / scale;
    
    zoomRect.origin.x    = center.x - (zoomRect.size.width  / 2.0);
    zoomRect.origin.y    = center.y - (zoomRect.size.height / 2.0);
    
    return zoomRect;
}

#pragma mark UIScrollView delegate methods
- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    return self.receiptImgView;
}


#pragma mark Receipt Actions
- (void)showReceiptWebView
{
    if (self.pdfWebView == nil)
    {
        [self.scrollView setScrollEnabled:NO];
        [self.scrollView removeFromSuperview];
        self.pdfWebView = [[UIWebView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height-self.tbar.frame.size.height)];
        self.pdfWebView.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    }
    
    NSData *pdfData = self.receipt.pdfData;
    
    [self.pdfWebView loadData:pdfData MIMEType:@"application/pdf" textEncodingName:@"UTF-8" baseURL:nil];
    [self.pdfWebView setScalesPageToFit:YES];
    [self.view addSubview:self.pdfWebView];
}

-(BOOL) shouldHideReceiptStore
{
    bool hideReceiptStore = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"HIDE_RECEIPT_STORE" withType:@"CTE_EXPENSE_ADMIN"]];
    return (hideReceiptStore || !self.canUseReceiptStore);
}

- (void)updateReceiptSelectedImpl
{
    // MOB-13306 wipe old data on update, just so pdfs are refreshed
    [self clearReceiptFromCacheByReceiptId];
    
    NSString *receiptStoreOption = nil;
    
    if (self.receiptActionSheet != nil)
    {
        self.receiptActionSheet = nil;
    }
    
    bool hideReceiptStore = [self shouldHideReceiptStore];
    
    if (self.canUseReceiptStore && !hideReceiptStore)
    {
        receiptStoreOption = [Localizer getLocalizedText:@"Receipt Store"];
    }
    
    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
	{
        if (receiptStoreOption != nil)
        {
            if ([ExSystem is8Plus] && [UIDevice isPad]){
                _alertController = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
                [self setCameraAlertView];
                [self setPhotoAlbumAlertView];
                [self setReceiptStoreAlertView];
                [self showAlertView];
            } else {
                self.receiptActionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                                          delegate:self
                                                                 cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                                            destructiveButtonTitle:nil
                                                                 otherButtonTitles:[Localizer getLocalizedText:@"Camera"],
                                           [Localizer getLocalizedText:@"Photo Album"],
                                           receiptStoreOption,
                                           nil];
            }
        }
        else // no receipt store
        {
            if ([ExSystem is8Plus] && [UIDevice isPad]){
                [self setCameraAlertView];
                [self setPhotoAlbumAlertView];
                [self showAlertView];
            } else {
                self.receiptActionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                                          delegate:self
                                                                 cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                                            destructiveButtonTitle:nil
                                                                 otherButtonTitles:[Localizer getLocalizedText:@"Camera"],
                                           [Localizer getLocalizedText:@"Photo Album"],
                                           nil];
            }
        }
    }
    else
    {
        if (receiptStoreOption != nil)
        {
            if ([ExSystem is8Plus] && [UIDevice isPad]){
                _alertController = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
                [self setPhotoAlbumAlertView];
                [self setReceiptStoreAlertView];
                [self showAlertView];
            } else {
                self.receiptActionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                                          delegate:self
                                                                 cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                                            destructiveButtonTitle:nil
                                                                 otherButtonTitles:[Localizer getLocalizedText:@"Photo Album"],
                                           receiptStoreOption,
                                           nil];
            }
        }
        else
        {
            if ([ExSystem is8Plus] && [UIDevice isPad]){
                _alertController = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
                [self setPhotoAlbumAlertView];
                [self showAlertView];
            } else {
                self.receiptActionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                                          delegate:self
                                                                 cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                                            destructiveButtonTitle:nil
                                                                 otherButtonTitles:[Localizer getLocalizedText:@"Photo Album"],
                                           nil];
            }
        }
    }
    
    if(![ExSystem is8Plus] && [UIDevice isPad])
	{
        CGRect rect = self.navigationController.navigationBar.frame;
		[self.receiptActionSheet showFromRect:rect inView:self.view animated:YES];
	}
	else
    {
        self.receiptActionSheet.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
        [self.receiptActionSheet showFromToolbar:self.tbar];
    }
    
}

-(void)setCameraAlertView
{
    UIAlertAction *camera = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Camera"]
                                                     style:UIAlertActionStyleDefault
                                                   handler:^(UIAlertAction *action){
                                                       DLog(@"camera clicked");
                                                       isCameraSelected = TRUE;
                                                       [self cameraSelected];
                                                   }];
    [self.alertController addAction:camera];
}

-(void)setPhotoAlbumAlertView
{
    UIAlertAction *photoAlbum = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Photo Album"]
                                                         style:UIAlertActionStyleDefault
                                                       handler:^(UIAlertAction *action){
                                                           DLog(@"photo album clicked");
                                                           isCameraSelected = FALSE;
                                                           [self photoAlbumSelected];
                                                       }];
    [self.alertController addAction:photoAlbum];
}

-(void)setReceiptStoreAlertView
{
    UIAlertAction *receiptStore = [UIAlertAction actionWithTitle:[Localizer getLocalizedText:@"Receipt Store"]
                                                           style:UIAlertActionStyleDefault
                                                         handler:^(UIAlertAction *action){
                                                             DLog(@"receipt store clicked");
                                                             [self receiptStoreSelected];
                                                         }];
    [self.alertController addAction:receiptStore];
}

- (void)showAlertView
{
    CGRect rect = CGRectMake(self.navigationController.navigationBar.frame.size.width / 2, self.navigationController.navigationBar.frame.size.height, 1, 1);

    [self.alertController setModalPresentationStyle:UIModalPresentationPopover];
    [self.alertController.popoverPresentationController setSourceView:[self view]];
    [self.alertController.popoverPresentationController setSourceRect:rect];
    [self.alertController.popoverPresentationController setPermittedArrowDirections:UIPopoverArrowDirectionUp];
    [self presentViewController:self.alertController animated:YES completion:nil];
}

- (void)appendReceiptSelected
{
    self.appendSelected = YES;
    [self updateReceiptSelectedImpl];
}

- (void)updateReceiptSelected
{
    self.appendSelected = NO;
    [self updateReceiptSelectedImpl];
}

- (void)cameraSelected
{
    if([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera])
    {
        [Flurry logEvent:@"Selected receipt image using camera"];
        
        if ([Config isGov])
        {
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
        else{
            // Use overlay on camera view
            _picker = [[CCImagePickerViewController alloc] init];
            self.picker.isToShowImagePickerView = YES;
            
            __weak ReceiptEditorVC *weakSelf = self;
            [self.picker setCancel:^(CCImagePickerViewController *picker) {
                [weakSelf dismissViewControllerAnimated:YES completion:nil];
            }];
            
            [self.picker setRetake:^(CCImagePickerViewController *picker, NSDictionary *info) {
                [weakSelf dismissViewControllerAnimated:NO completion:^{
                    [weakSelf presentViewController:picker animated:YES completion:nil];
                }];
            }];
            
            [self.picker setDone:^(CCImagePickerViewController *picker, NSDictionary *info) {
                UIImage* smallerImage = [picker restrictImageSize:[info objectForKey:UIImagePickerControllerOriginalImage]];
                [weakSelf didTakePicture: smallerImage];
                [[UIApplication sharedApplication] setStatusBarHidden:NO];
                [weakSelf dismissViewControllerAnimated:YES completion:nil];
            }];
            [self performSelector:@selector(presentPickerViewController:) withObject:self.picker afterDelay:0.1];
        }
    }
}

-(void)presentPickerViewController:(CCImagePickerViewController *)picker
{
    [self presentViewController:picker animated:YES completion:nil];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
    
    if ([self.pickerPopOver isPopoverVisible] && [self.pickerPopOver.contentViewController isKindOfClass:[UIImagePickerController class]])
    {
        [self.pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
}

- (void)photoAlbumSelected
{
    if([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypePhotoLibrary])
    {
        NSDictionary *dict = @{@"Added Using": @"Album"};
        [Flurry logEvent:@"Receipts: Add" withParameters:dict];
        
        _picker = [[CCImagePickerViewController alloc] init];
        self.picker.albumSelected = YES;
        self.picker.isToShowImagePickerView = YES;
        
        __weak ReceiptEditorVC *weakSelf = self;
        
        [self.picker setCancel:^(CCImagePickerViewController *picker) {
            weakSelf.picker.isToShowImagePickerView = NO;
            if (weakSelf.pickerPopOver !=nil)
                [weakSelf.pickerPopOver dismissPopoverAnimated:YES];
            else
                [weakSelf dismissViewControllerAnimated:YES completion:nil];
        }];
        
        [self.picker setRetake:^(CCImagePickerViewController *picker, NSDictionary *info) {
            if ([UIDevice isPad])
            {
                [weakSelf.pickerPopOver dismissPopoverAnimated:YES];
                [weakSelf cameraSelected];
            } else {
                [weakSelf dismissViewControllerAnimated:NO completion:^{
                    [weakSelf presentViewController:picker animated:YES completion:nil];
                }];
            }

        }];
        
        [self.picker setDone:^(CCImagePickerViewController *picker, NSDictionary *info) {
            UIImage* smallerImage = [picker restrictImageSize:[info objectForKey:UIImagePickerControllerOriginalImage]];
            [weakSelf didTakePicture: smallerImage];
            if ([UIDevice isPad]){
                [weakSelf.pickerPopOver dismissPopoverAnimated:YES];
            }
            else{
                [[UIApplication sharedApplication] setStatusBarHidden:NO];
                [weakSelf dismissViewControllerAnimated:YES completion:nil];
            }
        }];
        
        if ([UIDevice isPad])
        {
            [self performSelector:@selector(presentPopover:) withObject:self.picker afterDelay:0.1];
        }
        else
            [self presentViewController:self.picker animated:YES completion:nil];
    }
}

-(void)presentPopover:(CCImagePickerViewController *)picker
{
    picker.picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:picker];
    CGRect rect = CGRectMake(self.view.frame.size.width / 2, 0, 1, 1);
    [self.pickerPopOver presentPopoverFromRect:rect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
}

- (UIModalPresentationStyle) adaptivePresentationStyleForPresentationController: (UIPresentationController * ) controller {
    return UIModalPresentationNone;
}

-(void)deleteLocalReceipt
{
    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Confirm Detach"]
                                                        message:[Localizer getLocalizedText:@"CONFIRM_DETACH_TEXT"]
                                                       delegate:self
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                              otherButtonTitles:[Localizer getLocalizedText:@"OK"],
                          nil];
    alert.tag = kAlertViewDeleteReceipt;
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

#pragma mark UIActionSheet Delegate methods
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
	BOOL hasCamera = [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera];
	int offset = hasCamera? 1 : 0;
	
	if (hasCamera && (buttonIndex ==(-1 + offset)))
	{
        isCameraSelected = TRUE;
		[self cameraSelected];
	}
	else if (buttonIndex == (0 + offset))
	{
        isCameraSelected = FALSE;
		[self photoAlbumSelected];
	}
	else if (buttonIndex == (1 + offset))
	{
        bool hideReceiptStore = [self shouldHideReceiptStore];
        if (self.canUseReceiptStore && !hideReceiptStore)
        {
            [self receiptStoreSelected];
        }
        else
        {
            [actionSheet dismissWithClickedButtonIndex:buttonIndex animated:YES];
        }
	}
    else
    {
        [actionSheet dismissWithClickedButtonIndex:buttonIndex animated:YES];
    }
}

#pragma mark Alert View Delegate Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == kAlertViewDeleteReceipt && buttonIndex == 1)
    {
        [self.delegate receiptDeleted:self.receipt];
        [self closeMe];
    }
    if(alertView.tag == kAlertViewEReceiptWithoutReceipt && buttonIndex == 0){
        [self closeMe];
    }
}

#pragma mark NoDataViewDelegate method
- (BOOL)adjustNoDataView:(NoDataMasterView*) negView
{
    // Return whether to hide toolbar
    return NO;
}

-(BOOL)canShowActionOnNoData
{
    return self.canUpdate || self.canAppend;
}

- (NSString*) buttonTitleForNoDataView
{
    return [@"Add Receipt" localize];
}

- (NSString*) titleForNoDataView
{
    return [@"NO_RECEIPT_NEG" localize];
}

-(BOOL) canShowOfflineTitleForNoDataView
{
    return (!self.supportsOffline);
}

-(BOOL) allowActionWhileOffline
{
    return self.supportsOffline;
}

- (void)actionOnNoData:(id)sender
{
    [self updateReceiptSelected];
}

- (NSString *)instructionForNoDataView
{
    return nil;
}

-(NSString*) imageForNoDataView
{
    return @"neg_receipt_icon";
}

#pragma receipt error handling
-(BOOL) handleImageConfigError:(NSString*) errCode
{
    if ([@"Imaging Configuration Not Available." isEqualToString: errCode])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Cannot access receipt"]
                              message:[Localizer getLocalizedText:@"ERROR_BAD_CONFIG_MSG"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        [alert show];
        return TRUE;
    }
    return FALSE;
}

-(void) handleReceiptFailure
{
    // MOB-9416 Dimiss wait view, etc.
    if ([self isViewLoaded]) {
        [self hideWaitView];
    }
    
    // Revert to NoDataView or old image
    self.receipt.receiptImg = self.receiptImgView.image;
    if (self.receipt.receiptImg == nil)
        [self showNoDataView:self];
}

#pragma mark Load receipts
// TODO - Rid of this one, move to downloadHelper
-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:GET_RECEIPT_URL])
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
            // TODO - more error handling
        }
        else
        {
            if (rUrl != nil && rUrl.receiptUrl != nil)
            {
                self.receipt.url = rUrl.receiptUrl;
                self.receipt.dataType = rUrl.fileType;
                [self downloadReceipt];
            }
        }
    }
    else if ([msg.idKey isEqualToString:APPEND_RECEIPT])
    {
        if ([self isViewLoaded])
            [self hideWaitView];
        
        AppendReceiptData* arData = (AppendReceiptData*)msg.responder;
        if (msg.errBody != nil || ![arData.actionStatus.status isEqualToString:@"SUCCESS"])
        {
            NSString* errMsg = msg.errBody != nil ? msg.errBody : nil;
            
            if(errMsg == nil)
                errMsg = [Localizer getLocalizedText:@"Cannot append receipt"];
            
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
            [self receiptAppendSucceeded:self.receipt.receiptId];
        }
    }
    else if([msg.idKey isEqualToString:SAVE_REPORT_RECEIPT2])
    {
        if([self isWaitViewShowing])
        {
            [self hideWaitView];
            [self closeMe];
        }
        [self.receiptEditorDataModel getReportData].receiptImageAvailable = @"Y";
    }
}

-(void) retrieveReceiptUrl
{
    // MOB-21302
    // Add a hack to solve the problem of receipt ID
    if([self.receipt.receiptId isEqualToString:@"HACK e-receipt Image ID"]){

    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"ReceiptImageUnavailableTitle"]
                                                message:[Localizer getLocalizedText:@"ReceiptImageUnavailable"]
                                                delegate:self
                                                cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                                otherButtonTitles:nil];

        alert.tag = kAlertViewEReceiptWithoutReceipt;
        [AnalyticsTracker logEventWithCategory:@"All Mobile Expenses" eventAction:@"E-Receipt Image Error" eventLabel:nil eventValue:nil];
        [alert show];
        [self handleReceiptFailure];

        return;
    }

    // For report entry receipts
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReceiptImageUrl/%@", [ExSystem sharedInstance].entitySettings.uri, self.receipt.receiptId];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",nil];
    [[ExSystem sharedInstance].msgControl createMsg:GET_RECEIPT_URL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) downloadReceipt
{
    [self showLoadingView];
    // Start uploading the receipt
    [ReceiptDownloader downloadReceiptForId:[self.receipt fileCacheKey] dataType:self.receipt.dataType thumbNail:NO url:self.receipt.url delegate:self useHttpPost:self.receipt.useHttpPost];
}

// MOB-13306 need to reset the receipt data for this receipt
-(void) clearReceiptFromCacheByReceiptId
{
    [[ReceiptCache sharedInstance] deleteReceiptsMatchingId:[self.receipt fileCacheKey]];
    self.receipt.dataType = @"";
    self.receipt.pdfData = nil;
    self.receipt.receiptImg = nil;
}

-(void) loadReceiptFromCacheByReceiptId
{
    NSString *dataType = nil;
    NSData *imageData = [[ReceiptCache sharedInstance] getFullSizeReceiptForId:[self.receipt fileCacheKey] dataType:&dataType];
    if (imageData != nil)
    {
        if ([dataType  lengthIgnoreWhitespace]) {
            self.receipt.dataType = dataType;
        }
        
        if (self.receipt.dataType == nil) {
            self.receipt.dataType = @"";
        }
        
        if ([self.receipt isPDF]) {
            self.receipt.pdfData = imageData;
        }
        else{
            self.receipt.receiptImg = [UIImage imageWithData:imageData];
        }
    }
}

-(void) appendReceipt:(NSString*) newReceiptImageId
{
    [self showWaitView];
    // For report entry receipts
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 newReceiptImageId, @"FROM_RECEIPT_IMAGE_ID",
                                 self.receipt.receiptId, @"TO_RECEIPT_IMAGE_ID",
                                 nil];
    [[ExSystem sharedInstance].msgControl createMsg:APPEND_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


#pragma mark ReceiptDownloaderDelegate

-(void) didDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url
{
    [self loadReceiptFromCacheByReceiptId];
    
    if (!self.isViewLoaded)
        return;
    
    [self hideLoadingView];
    
    if ([self.receipt isPDF])
    {
        [self showReceiptWebView];
        if ([self.delegate respondsToSelector:@selector(receiptDisplayed:)])
            [self.delegate receiptDisplayed:self.receipt];
    }
    else
    {
        if (self.receipt.receiptImg != nil)
        {
            [self adjustReceiptViewWithImage:self.receipt.receiptImg];
            if ([self.delegate respondsToSelector:@selector(receiptDisplayed:)])
                [self.delegate receiptDisplayed:self.receipt];
        }
        else
        {
            // ##TODO## Download error or ask the user to wait?
            [self showNoDataView:self];
        }
    }
}

-(void) didFailToDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url
{
    if ([self isViewLoaded])
        [self hideWaitView];
    [self showNoDataView:self];
    
    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Failed to download receipt"]
                                                        message:nil
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                              otherButtonTitles:
                          nil];
    [alert show];
    
}

#pragma mark ReceiptUploaderDelegate methods
-(void) failedToPrepareImageData // memory issue
{
    if ([self isViewLoaded])
        [self hideWaitView];
    
    NSDictionary *dict = @{@"Failure": @"Failed to capture or reduce resolution for receipt image"};
    [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
    
    // MOB-9416 handle image conversion failure b/c memory shortage
    NSString *errMsg = [Localizer getLocalizedText:@"Free up memory and retry receipt upload"];
    
    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"]
                                                        message:errMsg
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                              otherButtonTitles:
                          nil];
    [alert show];
    
    [self handleReceiptFailure];
}

-(void) failedToUploadImage:(NSString*) errorStatus // e.g. Imaging not configured
{
    if ([self isViewLoaded])
        [self hideWaitView];
    
    // Flurry?
    if (![self handleImageConfigError:errorStatus])
    {
        NSString *errMsg = errorStatus != nil? errorStatus :
        [Localizer getLocalizedText:@"ReceiptUploadFailMsg"];
        
        UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"]
                                                            message:errMsg
                                                           delegate:nil
                                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                                  otherButtonTitles:
                              nil];
        [alert show];
        
        // If offline is supported, then queue the receipt that failed to upload
        if ([self supportsOffline])
        {
            [[MCLogging getInstance] log:@"Receipt upload failed and queue the receipt for upload later" Level:MC_LOG_DEBU];
            [self queueReceipt:self.receipt.receiptImg];
        }
        else
        {
            [[MCLogging getInstance] log:@"Receipt upload failed and no offline support" Level:MC_LOG_DEBU];
            [self handleReceiptFailure];
        }
    }
    else
    {
        // Receipt Configuration error.
        [[MCLogging getInstance] log:@"Receipt upload failed because of receipt configuration error" Level:MC_LOG_DEBU];
        [self handleReceiptFailure];
    }
}

-(void) receiptUploadSucceeded:(NSString*) receiptImageId
{
    NSString *ID = [self.receipt fileCacheKey];
    if ([ID length])
        [[ReceiptCache sharedInstance] deleteReceiptsMatchingId:[self.receipt fileCacheKey]];
    
    // Allow delegate to put up a waitview for further processing
    if (!self.appendSelected)
    {
        if ([self isViewLoaded])
            [self hideWaitView];
        
        self.receipt.receiptId = receiptImageId;
        [self.delegate receiptUpdated:self.receipt useV2Endpoint:false];
        [self closeMe];
    }
    else
    {
        // Append receipt, and then close the view
        [self appendReceipt:receiptImageId];
    }
}

-(void) receiptAppendSucceeded:(NSString*) receiptImageId
{
    // Remove the current receipt from cache
    [[ReceiptCache sharedInstance] deleteReceiptsMatchingId:[self.receipt fileCacheKey]];
    
    // ##TODO## Remove the hack.  The downloader should know the data type
    self.receipt.dataType = MIME_TYPE_PDF;
    
    // set receipt image id, but wipe out cached image/pdf data
    self.receipt.receiptId = receiptImageId;
    self.receipt.receiptImg = nil;
    self.receipt.pdfData = nil;
    
    [self.delegate receiptUpdated:self.receipt useV2Endpoint:false];
    [self closeMe];
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
	} else {
		[[[UnifiedImagePicker sharedInstance] imagePicker] dismissViewControllerAnimated:YES completion:nil];
    }
    
	self.receipt.receiptImg = image;
    [self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
    
    if([self.delegate isKindOfClass:[ReportDetailViewController class]])
    {
        [self receiptUpdated:self.receipt];
    }
    else if ([self shouldAttemptUpload])
    {
        self.uploader = [[ReceiptUploader alloc] init];
        [self.uploader setSeedData:self withImage:self.receipt.receiptImg];
        [self.uploader startUpload];
    }
    else
    {
        [self hideWaitView];
        [self queueReceipt:image];
    }
}

#pragma mark ReceiptCameraOverlayVCDelegate methods
- (void)didTakePicture:(UIImage *)image
{
    NSDictionary *dict = @{@"Added Using": @"Camera"};
    [Flurry logEvent:@"Receipts: Add" withParameters:dict];
    
    // Hold on to the image and then dismiss the camera
	self.receipt.receiptImg = image;
    [self didFinishWithCamera];
    
    [self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
    
    if([self.delegate isKindOfClass:[ReportDetailViewController class]])
    {
        [self receiptUpdated:self.receipt];
    }
    else if ([self shouldAttemptUpload])
    {
        self.uploader = [[ReceiptUploader alloc] init];
        [self.uploader setSeedData:self withImage:self.receipt.receiptImg];
        [self.uploader startUpload];
    }
    else
    {
        [self hideWaitView];
        [self queueReceipt:image];
    }
}

- (void)didFinishWithCamera
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

		// MOB-15134 - dont see a reason why this is required.
		// Calling VC should ideally handle it. Can be deleted if there is not impact by jan-2014
        
		// MOB-15975
        // Uncomment this line. Because receipt view doesn't dismiss after taking a picture
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

- (BOOL)shouldDisplayAlbum
{
    return NO;
}

-(void) queueReceipt:(UIImage*)receiptImage
{
    EntityUploadQueueItem *latestReceiptQueueItem = [ReceiptEditorVC queueReceiptImage:receiptImage date:[NSDate date]];
    self.receipt.localReceiptId = latestReceiptQueueItem.entityInstanceId;
    [self receiptQueued];
}

+(EntityUploadQueueItem *) queueReceiptImage:(UIImage*)receiptImage date:(NSDate*)creationDate
{
    // Create an id for the local receipt
    NSString *newLocalReceiptImageId = [PostMsgInfo getUUID]; // Happens to be a UUID.  It could be anything.
    
    // Get the NSData object from the UIImage
    NSData *newLocalReceiptImageData = [self receiptImageDataFromReceiptImage:receiptImage];
    
    // Save the receipt to disk
    NSString *filePath = [UploadableReceipt filePathForLocalReceiptImageId:newLocalReceiptImageId isPdfReceipt:NO];
    [newLocalReceiptImageData writeToFile:filePath atomically:YES];
    
    // Queue the receipt
    return [UploadQueue queueItemWithId:newLocalReceiptImageId entityTypeName:@"Receipt" creationDate:creationDate];
}

// MOB-21462:Save PDF receipt to local
+(EntityUploadQueueItem *) queuePdfReceipt:(NSData*)pdfData date:(NSDate*)creationDate
{
    // Create an id for the local receipt
    NSString *newLocalReceiptImageId = [PostMsgInfo getUUID]; // Happens to be a UUID.  It could be anything.
    
    // Save the receipt to disk
     NSString *filePath = [UploadableReceipt filePathForLocalReceiptImageId:newLocalReceiptImageId isPdfReceipt:YES];
    [pdfData writeToFile:filePath atomically:YES];
    
    // Queue the receipt
    return [UploadQueue queueItemWithId:newLocalReceiptImageId entityTypeName:@"PdfReceipt" creationDate:creationDate];
}

-(void) receiptQueued
{

    [self.delegate receiptQueued:self.receipt];
    
    [self showLoadingViewWithText:[@"Saving offline receipt" localize]];
    double delayInSeconds = 3.0;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                [self closeMe];
        });
}

-(void) closeMe
{
    if (self.receiptActionSheet != nil) {
        [self.receiptActionSheet dismissWithClickedButtonIndex:0 animated:NO];
    }
    
    if ([UIDevice isPad])
    {
        if (isCameraSelected && ![ExSystem connectedToNetwork])
        {
            [self.navigationController popViewControllerAnimated:YES];
        }
        else if ([self.navigationController.viewControllers count] > 1)
            [self.navigationController popViewControllerAnimated:YES];
        else
        {
            [self dismissViewControllerAnimated:YES completion:nil];
        }
    }
    else{
        [self.navigationController popViewControllerAnimated:YES];
    }
}

#pragma mark ReceiptStoreViewDelegate method
-(void)didSelectImageFromReceiptStore:(ReceiptStoreReceipt*)receiptData
{
	[self dismissViewControllerAnimated:NO completion:nil];
    
    if (!self.appendSelected)
    {
        self.receipt.receiptId = receiptData.receiptImageId;
        self.receipt.receiptImg = receiptData.fullScreenImage;
        self.receipt.pdfData = receiptData.pdfData;
        self.receipt.dataType = receiptData.fileType; //convert?
    }
    
    [self receiptUploadSucceeded:receiptData.receiptImageId];
}

#pragma mark Helper APIs
- (BOOL)shouldAttemptUpload
{
    BOOL isOnline = [ExSystem connectedToNetwork];
    BOOL shouldAttempt = ((isOnline || !self.supportsOffline) && !self.mustQueue);
    return shouldAttempt;
}


+(NSData *) receiptImageDataFromReceiptImage:(UIImage*)receiptImage
{
    // MOB-10877 For iOS 6 only, receiptImage.imageOrientation is 3 - UIImageOrientationRight.
    if ([ExSystem is6Plus])
    {
        UIImage* correctedImage = nil;
        if(UIImageOrientationRight == [receiptImage imageOrientation])
        {
            correctedImage = [ImageUtil fixOrientation:receiptImage];
            if (correctedImage != nil)
                receiptImage = correctedImage;
        }
    }
    
    NSData *imgData = nil;
	if (receiptImage != nil)
	{
        imgData = UIImageJPEGRepresentation(receiptImage, 0.9f);
        //        imgData = nil;//###MOB-9416##test only
    }
    
    return imgData;
}

// AJC - MOB-13542 - this is terrible and i apologize. the alternative is that it just doesn't work
#pragma mark ReceiptEditorDelegate
-(void) receiptUpdated:(Receipt*) rcpt
{
    ReportData* report = [self.receiptEditorDataModel getReportData];
    
    if (rcpt != nil )
    {
        report.receiptImageId = rcpt.receiptId;

        NSString* dataType = rcpt.dataType;

        NSData* fileData = nil;
        NSString* mimeType = @"";

        if([dataType caseInsensitiveCompare:JPG] == NSOrderedSame)
        {
            // pngs and jpgs are both called just "jpg"
            // other areas of the code treat images generically and it works with the server
            fileData = UIImageJPEGRepresentation(rcpt.receiptImg, 0.9f);
            mimeType = MIME_TYPE_JPG;
        }
        else if ([dataType caseInsensitiveCompare:PDF] == NSOrderedSame)
        {
            fileData = rcpt.pdfData;
            mimeType = MIME_TYPE_PDF;
        }
        else
        {
            NSLog(@"!!! This case should never be hit  Datatype : %@!!!", dataType);
            
            // if dataType is not set - as it previously was not in all places - treating the incoming data like an image
            // which it would be, because that was the only pathway previously existing
            fileData = UIImageJPEGRepresentation(rcpt.receiptImg, 0.9f);
            mimeType = MIME_TYPE_JPG;
        }
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:report, @"REPORT", [self getViewIDKey], @"TO_VIEW", mimeType, @"MIME_TYPE", fileData, @"FILE_DATA", nil];
        [[ExSystem sharedInstance].msgControl createMsg:SAVE_REPORT_RECEIPT2 CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

#pragma mark -
#pragma mark Image Scaling
// Copied from UnifiedImagePicker.delayedImageManipulationForSize
-(UIImage*)restrictImageSize:(UIImage*)originalImage
{
    UIImage* result = originalImage;
    
    int baseScaler = 1000;
    float scaler = 1;
    float w = originalImage.size.width;
	float h = originalImage.size.height;
    
    if(w <= 0)
		w = 1;
    
    if (h >= w) {
        scaler = h/baseScaler;
    }
    else {
        scaler = w/baseScaler;
    }
	
    if (w >= baseScaler || h >= baseScaler) {
        
        h = h/scaler;
        w = w/scaler;
        
        CGSize newSize = CGSizeMake(w , h);
        UIImage * img = nil;
        img = [ImageUtil imageWithImage:originalImage scaledToSize:newSize];
        result = img;
#ifdef TEST_LOG
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"Image size before: %@", NSStringFromCGSize(originalImage.size)] Level:MC_LOG_DEBU];
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"Image size after: %@", NSStringFromCGSize(result.size)] Level:MC_LOG_DEBU];
#endif
        
    }
    
    return result;
}
// AJC - MOB-13542 - this is terrible and i apologize. the alternative is that it just doesn't work

@end

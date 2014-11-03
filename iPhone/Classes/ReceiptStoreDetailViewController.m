//
//  ReceiptStoreDetailViewController.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/9/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReceiptStoreDetailViewController.h"
#import "ExSystem.h" 

#import "ReceiptStoreListView.h"
#import "RequestController.h"
#import "ExSystem.h"
#import "UploadableReceipt.h"
#import "ReceiptDownloader.h"
#import "ReceiptCache.h"
#import "Config.h"

#import "GetReceiptUrl.h"

@interface ReceiptStoreDetailViewController (Private)
-(void)deleteReceipt;
@end

@implementation ReceiptStoreDetailViewController
@synthesize tBar, receiptImageView,receiptDelegate,receiptData,localReceiptId,chooseBtn,scrollView;
@synthesize pdfWebView,isPDF,pdfData;

-(NSString *)getViewIDKey
{
	return RECEIPT_STORE_DETAIL_VIEW;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

#pragma mark - Instantiation
+(ReceiptStoreDetailViewController*) receiptStoreDetailViewControllerForLocalReceiptId:(NSString*)receiptId
{
    // Navigation logic may go here. Create and push another view controller.
    ReceiptStoreDetailViewController *receiptDetailVC = [[ReceiptStoreDetailViewController alloc] initWithNibName:@"ReceiptStoreDetailViewController" bundle:nil];
    receiptDetailVC.title = [Localizer getLocalizedText:@"Receipt"];
    receiptDetailVC.localReceiptId = receiptId;
    return receiptDetailVC;
}

-(void) initReceiptWebView
{
    if (pdfData != nil) 
    {
        // MOB-16498 it appears the scrollView doesn't detect an iPad modal view or iPhone 5 size correctly
        // There is probably a xib somewhere with this set to iPhone 4 size...
        int height = 504;
        int width = 320;

        if ([UIDevice isPad]) {
            height = 576;
            width = 540;
        }

        self.pdfWebView = [[UIWebView alloc] initWithFrame:CGRectMake(0, 0, width, height)];
        
        [pdfWebView loadData:pdfData MIMEType:@"application/pdf" textEncodingName:@"UTF-8" baseURL:nil];
        [pdfWebView setScalesPageToFit:YES];
        [scrollView addSubview:pdfWebView];
    }
}

-(void) chooseImage
{
    receiptData.fullScreenImage = receiptImageView.image;
    receiptData.pdfData = self.pdfData;
    receiptData.pdfData = self.pdfData;
    
	if (self.receiptDelegate != nil && [(UIViewController*)receiptDelegate respondsToSelector:@selector(didSelectImageFromReceiptStore:)]) 
	{
		[receiptDelegate didSelectImageFromReceiptStore:self.receiptData];
	}
	
	[self.navigationController popViewControllerAnimated:NO];
}

/**
 Handle the reciept url response
 */
- (void)handleReceiptUrlRespose:(Msg *)msg
{
    GetReceiptUrl* rUrl = (GetReceiptUrl *)msg.responder;
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
    } else {

        if (rUrl != nil && rUrl.receiptUrl != nil)
        {
            self.receiptData.imageUrl = rUrl.receiptUrl;
            self.receiptData.fileType = rUrl.fileType;
            [self downloadReceipt];
        } else {
            [self showNoDataView:self];
        }
    }
}

-(void) respondToFoundData:(Msg *)msg
{
    if([msg.idKey isEqualToString:@"GET_RECEIPT_URL"])
    {
        [self handleReceiptUrlRespose:msg];
    }
    else if([msg.idKey isEqualToString:@"DELETE_RECEIPT"])
    {
        if ([self isViewLoaded]) 
		{
            [self hideWaitView];
            [self.navigationController popViewControllerAnimated:YES];
        }
    }
}

#pragma mark -
#pragma mark View methods
- (void) viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void) viewDidLoad {
    [super viewDidLoad];

	if(self.receiptDelegate != nil)
	{
		self.chooseBtn = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Select"] style:UIBarButtonItemStyleBordered target:self action:@selector(chooseImage)];
		UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
		[self setToolbarItems:@[flexibleSpace,chooseBtn]];
        [self.navigationController setToolbarHidden:NO];
	}
    
    scrollView.delegate = self;
    
    UIImage *receiptImage = nil;
    if (self.localReceiptId != nil)
    {
        receiptImage = [UploadableReceipt imageForLocalReceiptImageId:self.localReceiptId];
    }
    else
    {
        NSString *dataType = nil;
        NSData *imageData = [[ReceiptCache sharedInstance] getFullSizeReceiptForId:receiptData.receiptImageId dataType:&dataType];
        
        if (imageData != nil)
        {
            if (dataType != nil && [dataType caseInsensitiveCompare:@"pdf"] == NSOrderedSame)
            {
                isPDF = YES;
                self.pdfData = imageData;
                [self initReceiptWebView];
            }
            else
                receiptImage = [UIImage imageWithData:imageData];
        }
        else
        {
            if ([ExSystem connectedToNetwork])
            {
                // MOB-13328 Receipt Urls expire quickly, about 15 mins.  So we have to keep getting them until Wanny gets a new service implemented.
                [self getReceiptUrlFromServer];
            }
            else
            {
                [self showOfflineView:self];
            }
        }
    }
    
	if (receiptImage != nil)
	{
		[self adjustReceiptViewWithImage:receiptImage];
	}
}


/**
 Need to ask for the receipt url cause urls expire every 30 mins... Why do you do this to me imaging team?
 */
- (void)getReceiptUrlFromServer
{
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Receipt Image(s)"]];

    // For report entry receipts
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReceiptImageUrl/%@", [ExSystem sharedInstance].entitySettings.uri, receiptData.receiptImageId];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",nil];
    [[ExSystem sharedInstance].msgControl createMsg:GET_RECEIPT_URL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

/**
 Download the Receipt
 */
- (void)downloadReceipt
{
    BOOL attemptingDownload = [ReceiptDownloader downloadReceiptForId:receiptData.receiptImageId dataType:receiptData.fileType thumbNail:NO url:receiptData.imageUrl delegate:self];

    if (attemptingDownload) {
        [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Receipt Image(s)"]];
    }
}

#pragma mark - ReceiptDownloaderDelegate Methods

-(void) didDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url
{
    if ([self isViewLoaded])
    {
        [self hideLoadingView];
        
        NSString *dataType = nil;
        NSData *imageData = [[ReceiptCache sharedInstance] getFullSizeReceiptForId:receiptId dataType:&dataType];
        
        if (imageData != nil)
        {
            if (dataType != nil && [dataType caseInsensitiveCompare:@"pdf"] == NSOrderedSame)
            {
                isPDF = YES;
                self.pdfData = imageData;
                [self initReceiptWebView];
            }
            else
            {
                UIImage *image = [UIImage imageWithData:imageData];
                [self adjustReceiptViewWithImage:image];
            }
        }
    }
}

-(void) didFailToDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url
{
    if ([self isViewLoaded])
        [self hideWaitView];

    if (![ExSystem connectedToNetwork])
        [self showOfflineView:self];
    else
    {
        [self showNoDataView:self];
        
        UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Failed to download receipt"]
                                                            message:nil
                                                           delegate:nil
                                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                                  otherButtonTitles:
                              nil];
        [alert show];
    }
}

#pragma mark -
#pragma mark Image scaling methods

-(void) initScaling
{
	if (receiptImageView.image != nil)
	{
		UIImage *myimage = receiptImageView.image;
		int w = myimage.size.width;
		int h = myimage.size.height;
		float screenW = 320;
		float screenH = 480;
				
		if([UIDevice isPad])
		{
			screenH = 620 - 44;
			screenW = 540;
		}
		else
		{
			if([ExSystem isLandscape])
			{
				screenW = 480;
				screenH = 320 - 44;
			}
			else
			{
				screenH = 480 - 44;
				screenW = 320;
			}
		}
		
		receiptImageView.autoresizingMask = UIViewAutoresizingNone;
		
		float scaler = (float)w / screenW;
		if(w > screenW)
		{
			h = h / scaler;
			receiptImageView.frame = CGRectMake(0, 0, screenW, h);
		}
		else if (w < screenW && h > screenH)
		{
			scaler = (float)h / screenH;
			w = w / scaler;
			receiptImageView.frame = CGRectMake(0, 0, w, screenH);
		}
		else 
		{
			
			receiptImageView.frame = CGRectMake(0, 0, w, h);
		}
		
		scrollView.autoresizingMask = UIViewAutoresizingNone;
        if ([UIDevice isPad])
            scrollView.frame = CGRectMake(0, 44, screenW, screenH);
        else
            scrollView.frame = CGRectMake(0, 0, screenW, screenH);
        
		receiptImageView.image = myimage;
		
		if(w < screenW)
			scrollView.contentSize = CGSizeMake(w, h);
		else 
			scrollView.contentSize = CGSizeMake(screenW, h);
		
		scrollView.maximumZoomScale = 8.0;
		// MOB-11244
		scrollView.minimumZoomScale = 1.0;
		scrollView.clipsToBounds = YES;
		scrollView.delegate = self;
	}
}


-(void)adjustReceiptViewWithImage:(UIImage*)myimage
{
	if (myimage != nil)
	{
		int w = myimage.size.width;
		int h = myimage.size.height;
		float screenW = self.view.frame.size.width;
		float screenH = self.view.frame.size.height;
		
        receiptImageView.contentMode = UIViewContentModeScaleAspectFit;
        receiptImageView.multipleTouchEnabled = YES;
        receiptImageView.backgroundColor = [UIColor clearColor];

		receiptImageView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
		scrollView.frame = CGRectMake(0, 0, screenW, screenH);
		
		scrollView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
		receiptImageView.image = myimage;
		
		scrollView.contentSize = CGSizeMake(w, h);
		scrollView.maximumZoomScale = 8.0;
		scrollView.minimumZoomScale = 1.0;
		scrollView.clipsToBounds = YES;
		scrollView.delegate = self;
        scrollView.backgroundColor = [UIColor clearColor];
        // MOB-11242
        // add gesture recognizers to the image view     
        receiptImageView.userInteractionEnabled = YES;
        scrollView.userInteractionEnabled = YES;
        UITapGestureRecognizer *doubleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleDoubleTap:)];
        [doubleTap setNumberOfTapsRequired:2];
        [receiptImageView addGestureRecognizer:doubleTap];
        
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


// Override to allow orientations other than the default portrait orientation.
- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations.
    return YES;
}


- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[self initScaling];
}


- (void) didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}


- (void) viewDidUnload {
    self.pdfData = nil;
    self.pdfWebView = nil;
    self.receiptImageView = nil;
    [super viewDidUnload];
}


#pragma mark Delete receipt action
-(void)deleteReceipt
{
    [self showWaitViewWithText:[Localizer getLocalizedText:@"Deleting receipt"]];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.receiptData.receiptImageId, @"ReceiptImageId",[self getViewIDKey], @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
    [[ExSystem sharedInstance].msgControl createMsg:DELETE_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark UIScrollView delegate methods
-(UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    if (!isPDF) 
    {
        return receiptImageView;
    }
    else 
    {
        return pdfWebView;
    }
}

@end

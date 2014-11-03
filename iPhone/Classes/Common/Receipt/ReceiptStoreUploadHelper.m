//
//  ReceiptStoreUploadHelper.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ReceiptStoreUploadHelper.h"
#import "EntityUploadQueueItem.h"
#import "ReceiptEditorVC.h"
#import "QuickExpensesReceiptStoreVC.h"
#import "ImageUtil.h"
#import "ReceiptCache.h"
#import "WaitViewController.h"

@interface ReceiptStoreUploadHelper()
{
    NSString* receiptUploadErrMsg;
}

@property (nonatomic, strong) ReceiptEditorVC *receiptEditorVC;
@property (nonatomic, strong) iPadHome9VC *homeVC;
//@property (nonatomic, strong) NSString        *receiptUploadErrMsg;
@property BOOL isToExpense;

@end

@implementation ReceiptStoreUploadHelper


- (id)init
{
    self = [super init];
    if (self) {
        self.openReceiptListWhenFinished = false;
    }
    return self;
}

- (void)startCamera:(UIBarButtonItem*)button
{
    self.picker = [[CCImagePickerViewController alloc] init];
    __weak ReceiptStoreUploadHelper *weakSelf = self;
    [self.picker setCancel:^(CCImagePickerViewController *picker) {
        [weakSelf.vc dismissViewControllerAnimated:YES completion:nil];
    }];
    
     [self.picker setRetake:^(CCImagePickerViewController *picker, NSDictionary *info) {
         weakSelf.isToExpense = FALSE;
         [weakSelf.vc dismissViewControllerAnimated:NO completion:^{
             [weakSelf.vc presentViewController:weakSelf.picker animated:YES completion:nil];
         }];
     }];

    [self.picker setDone:^(CCImagePickerViewController *picker, NSDictionary *info) {
        weakSelf.isToExpense = FALSE;
        [weakSelf.vc dismissViewControllerAnimated:YES completion:^{
            [[UIApplication sharedApplication] setStatusBarHidden:NO];
            UIImage* smallerImage = [weakSelf restrictImageSize:[info objectForKey:UIImagePickerControllerOriginalImage]];
            [weakSelf didTakePicture: smallerImage];
        }];
    }];

     [self.picker setExpense:^(CCImagePickerViewController *picker, NSDictionary *info) {
         [weakSelf.vc dismissViewControllerAnimated:YES completion:^{
             UIImage* smallerImage = [weakSelf restrictImageSize:[info objectForKey:UIImagePickerControllerOriginalImage]];
             [weakSelf showQuickExpense:smallerImage];
         }];
     }];
    self.picker.isToShowImagePickerView = YES;
    [weakSelf.vc presentViewController:self.picker animated:YES completion:nil];
}

- (void)showQuickExpense: (UIImage*)image
{
    QEFormVC *formVC = [[QEFormVC alloc] initWithEntryOrNil:nil withCloseButton:YES];
    formVC.title = [Localizer getLocalizedText:@"Add Expense"];
    self.isToExpense = YES;
    BOOL isOnline = [ExSystem connectedToNetwork];
    BOOL isNewExpense = YES;
    BOOL isExpenseQueued = [formVC isQueuedQuickExpense];
    BOOL allowReceiptEdits = (isOnline || isNewExpense || isExpenseQueued);
    BOOL willQueueExpense = (isExpenseQueued || (!isOnline && isNewExpense));

    // MOB-15134 -- Really hacky fix for crash on iOS7.
    __block BOOL isReceiptEditorOpen = FALSE;
    
    UIViewController *homevc;
    if ([UIDevice isPad])
    {
        if (self.vc.pickerPopOver != nil)
            [self.vc.pickerPopOver dismissPopoverAnimated:YES];

        homevc = [ConcurMobileAppDelegate findHomeVC];
        UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:formVC];
        //[self ResetBarColors:navi];
        if ([homevc respondsToSelector:@selector(ResetBarColors:)]) {
            [homevc performSelector:@selector(ResetBarColors:) withObject:navi];
        }
        navi.modalPresentationStyle = UIModalPresentationFormSheet;
        if (self.isFromReceiptStore)
        {
           [homevc dismissViewControllerAnimated:NO completion:nil];
        }

        [homevc presentViewController:navi animated:YES completion:NULL];
        // MOB-15134 -- Really hacky fix for crash on iOS7.
        [formVC showReceiptViewerAndAllowEdits:allowReceiptEdits excludeReceiptStoreOption:willQueueExpense];
    }
    else
    {
        [self.vc.navigationController pushViewController:formVC animated:NO];
        [formVC showReceiptViewerAndAllowEdits:NO excludeReceiptStoreOption:NO];

        //MOB-16954 : Dirty hack to delay the check so receiptvc gets some time to close gracefully.
        //TODO: The right fix should be to change QEformVC to handle upload receipt and create new expese instead of push and pop receiptEditorVC over QEFormVc
        double delayInSeconds = 0.5;
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
            if ([formVC.navigationController.topViewController isKindOfClass:[ReceiptEditorVC class]])
            {
                isReceiptEditorOpen = TRUE;
                self.receiptEditorVC = (ReceiptEditorVC*) self.vc.navigationController.topViewController;
            }
            if (isReceiptEditorOpen)
            {
                self.receiptEditorVC.receipt.receiptImg = image;
                self.receiptEditorVC.receipt.dataType = @"jpeg";
                [self.receiptEditorVC didTakePicture: image];
            }
        });
    }
    
    if ([UIDevice isPad])
    {
    // for iOS8, it needs one second delay to show the camera view. Have tried .5 second, but it does not work
        [self performSelector:@selector(uploadReceipt:) withObject:image afterDelay:1];
    }
   
    NSDictionary *dict = @{@"Came From": @"Camera"};
    [Flurry logEvent:@"Mobile Entry: Create2" withParameters:dict];
}

-(void)uploadReceipt:(UIImage*)image
{
    NSArray *viewControllers = nil;
    UIViewController *topVC = nil;

    UIViewController *homevc = [ConcurMobileAppDelegate findHomeVC];
    if (self.isFromReceiptStore && homevc.navigationController != nil)
    {
        viewControllers = homevc.navigationController.presentedViewController.childViewControllers;
    }
    else
    {
        viewControllers = self.vc.navigationController.presentedViewController.childViewControllers;
    }
    
    if (viewControllers != nil && [viewControllers count] >= 1)
    {
        int count = [viewControllers count];
        topVC = viewControllers[count - 1];
        
        if ([topVC isKindOfClass:[ReceiptEditorVC class]])
        {
            self.receiptEditorVC = (ReceiptEditorVC*) topVC;
            self.receiptEditorVC.showActionSheet = NO;
       		self.receiptEditorVC.receipt.receiptImg = image;
        	self.receiptEditorVC.receipt.dataType = @"jpeg";
       		[self.receiptEditorVC didTakePicture: image];
        }
    }
}

- (void)done
{
    self.uploader = nil;
    self.cameraOverlayVC = nil;
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

- (void)handleReceiptFailure
{
    [self done];
}

#pragma mark ReceiptUploaderDelegate methods
-(void) failedToPrepareImageData // memory issue
{
    if ([self.vc isViewLoaded])
        [self.vc hideWaitView];
    
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
    if ([self.vc isViewLoaded])
        [self.vc hideWaitView];
    
    // Flurry?
    if (![self handleImageConfigError:errorStatus])
    {
        receiptUploadErrMsg = errorStatus != nil? errorStatus :
        [Localizer getLocalizedText:@"ReceiptUploadFailMsg"];
        
        // If offline is supported, then queue the receipt that failed to upload
        [[MCLogging getInstance] log:@"Receipt upload failed and queue the receipt for upload later" Level:MC_LOG_DEBU];
        [self queueReceipt:self.uploader.receiptImage];
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
    // Allow delegate to put up a waitview for further processing
    if ([self.vc isViewLoaded])
        [self.vc hideWaitView];
 
    if (self.openReceiptListWhenFinished) {
        if ([UIDevice isPad]) {
            QuickExpensesReceiptStoreVC *nextController = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
            [nextController setSeedDataAndShowReceiptsInitially:YES allowSegmentSwitch:YES allowListEdit:YES];
            
            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
            
            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
            
            [localNavigationController setToolbarHidden:NO];
            [self.vc presentViewController:localNavigationController animated:YES completion:nil];
            
            
            // need to update the list after saving a receipt successfully
            if ([nextController respondsToSelector:@selector(reloadReceipts)]) {
                [nextController performSelector:@selector(reloadReceipts) withObject:nil];
            }
            
        }
        else if (self.isToExpense)
        {
            NSString *receiptId = [self.receiptEditorVC.receipt fileCacheKey];
            if ([receiptId length])
                [[ReceiptCache sharedInstance] deleteReceiptsMatchingId:[self.receiptEditorVC.receipt fileCacheKey]];
            
            self.receiptEditorVC.receipt.receiptId = receiptImageId;
            [self.receiptEditorVC.delegate receiptUpdated:self.receiptEditorVC.receipt useV2Endpoint:true];
            [self.vc.navigationController popViewControllerAnimated:NO];
        }
        else {
            QuickExpensesReceiptStoreVC *view = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
            [view setSeedDataAndShowReceiptsInitially:YES allowSegmentSwitch:YES allowListEdit:YES];
            [self.vc.navigationController pushViewController:view animated:YES];
        }
    }
    
    [self done];
    
    // need to update the list after saving a receipt successfully
    if ([self.vc respondsToSelector:@selector(reloadReceipts)]) {
        [self.vc performSelector:@selector(reloadReceipts) withObject:nil];
    }
}


#pragma mark ReceiptCameraOverlayVCDelegate methods
- (void)didTakePicture:(UIImage *)image
{
    [self didFinishWithCamera];
    
//    [self.vc showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
    
    BOOL isOnline = [ExSystem connectedToNetwork];
    if (isOnline)
    {
        self.uploader = [[ReceiptUploader alloc] init];
        [self.uploader setSeedData:self withImage:image];
        [WaitViewController showWithText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"] animated:YES];
        [self.uploader startUpload];
    }
    else
    {
        [self.vc hideWaitView];
        [self queueReceipt:image];
    }
}

- (void)didFinishWithCamera
{
	if ([UIDevice isPad])
	{
		if (self.vc.pickerPopOver != nil)
		{
			[self.vc.pickerPopOver dismissPopoverAnimated:YES];
			self.vc.pickerPopOver = nil;
		}
        // MOB-8441 Do not dismiss the image picker modally - it is always presented within popover for iPad
	}
	else
        [self.vc dismissViewControllerAnimated:YES completion:nil];
   
    [self done];
}

- (BOOL)shouldDisplayAlbum
{
    return YES;
}

#pragma mark queue

-(void) queueReceipt:(UIImage*)receiptImage
{
    [ReceiptEditorVC queueReceiptImage:receiptImage date:[NSDate date]];
    [self receiptQueued];
    [self done];
}

-(void) receiptQueued
{
    NSString *receiptQueuedMsg = [Localizer getLocalizedText:@"Your receipt has been queued"];
    NSString *msgTitle = nil;
    NSString *msgBody = nil;
    if (receiptUploadErrMsg != nil)
        msgBody = [NSString stringWithFormat:@"%@\r%@", receiptUploadErrMsg, receiptQueuedMsg];
    else
        msgBody = receiptQueuedMsg;
    
    if ([ExSystem connectedToNetwork])
        msgTitle = [Localizer getLocalizedText:@"Receipt upload failed"];
    else
        msgTitle = [Localizer getLocalizedText:@"Receipt Queued"];
    
    [self.vc.navigationController popViewControllerAnimated:NO];
    
    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:msgTitle
                                              message:msgBody
                                              delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                              otherButtonTitles:nil];
    [alert show];
}

#pragma mark - AlertView delegate methods
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (self.isToExpense)
    {
//        [vc.navigationController popViewControllerAnimated:NO];
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


@end

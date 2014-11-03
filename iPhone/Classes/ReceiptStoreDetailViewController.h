//
//  ReceiptStoreDetailViewController.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/9/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReceiptStoreReceipt.h"
#import "MobileViewController.h"
#import "ExSystem.h" 

#import "ReceiptDownloaderDelegate.h"

@protocol ReceiptStoreDelegate
-(void)didSelectImageFromReceiptStore:(ReceiptStoreReceipt*)receiptData;
@end


@interface ReceiptStoreDetailViewController : MobileViewController <UIScrollViewDelegate, ReceiptDownloaderDelegate> {
	UIToolbar					*tBar;
	UIScrollView				*scrollView;
	UIImageView					*receiptImageView;
	ReceiptStoreReceipt			*receiptData;
    NSString                    *localReceiptId;
	id<ReceiptStoreDelegate>	__weak receiptDelegate;
	UIBarButtonItem				*chooseBtn;
    
    BOOL                isPDF;
    UIWebView           *pdfWebView;
    NSData              *pdfData;  
}

@property (nonatomic,strong) IBOutlet 	UIToolbar		*tBar;
@property (nonatomic,strong) IBOutlet	UIScrollView	*scrollView;
@property (nonatomic,strong) IBOutlet 	UIImageView		*receiptImageView;
@property (nonatomic,weak) id<ReceiptStoreDelegate>	receiptDelegate;
@property (nonatomic,strong) ReceiptStoreReceipt		*receiptData;
@property (nonatomic,strong) NSString                   *localReceiptId;
@property (nonatomic,strong) UIBarButtonItem			*chooseBtn;

@property (nonatomic, assign) BOOL                          isPDF;
@property (nonatomic, strong) UIWebView                     *pdfWebView;
@property (nonatomic, strong) NSData                        *pdfData;

+(ReceiptStoreDetailViewController*) receiptStoreDetailViewControllerForLocalReceiptId:(NSString*)receiptId;

-(void) chooseImage;
-(void) adjustReceiptViewWithImage:(UIImage*)myimage;
-(void) initScaling;
@end

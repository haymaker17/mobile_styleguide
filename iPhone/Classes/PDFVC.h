//
//  PDFVC.h
//  TravelWallet
//
//  Created by Paul Kramer on 6/29/11.
//  Copyright 2011 pjkiosdevelopments. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MessageUI/MFMailComposeViewController.h>
//#import <DropboxSDK/DropboxSDK.h>
#import "MobileViewController.h"
#import "ReportData.h"

@interface PDFVC : MobileViewController <UIWebViewDelegate, MFMailComposeViewControllerDelegate, UIPrintInteractionControllerDelegate> //DBRestClientDelegate
{
    UIWebView *webView;
    NSString *pdfFilePath, *pdfFileName;
//    DBRestClient *restClient;
    int iUploadAmount, iUploadTotal;
    ReportData *report;
}

@property int iUploadAmount;
@property int iUploadTotal;
@property (strong, nonatomic) IBOutlet UIWebView *webView;
@property (strong, nonatomic) NSString *pdfFilePath;
@property (strong, nonatomic) NSString *pdfFileName;

@property (strong, nonatomic) ReportData *report;

-(void)showMailPDF:(id)sender;
#pragma mark - Close Methods
-(void) closeMe:(id)sender;
#pragma mark - Print
-(void) doPrint:(id)sender;

#pragma mark - Dropbox
-(IBAction)doDropbox:(id)sender;

-(void) uploadRawReceipts:(ReportData*) rpt;
@end

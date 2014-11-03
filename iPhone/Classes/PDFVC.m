//
//  PDFVC.m
//  TravelWallet
//
//  Created by Paul Kramer on 6/29/11.
//  Copyright 2011 pjkiosdevelopments. All rights reserved.
//

#import "PDFVC.h"
#import "ReportData.h"
#import "ReportReceiptInfoManager.h"
#import "Flurry.h"

@implementation PDFVC
@synthesize webView, pdfFilePath, pdfFileName, iUploadAmount, report, iUploadTotal;

-(NSString*) getViewIDKey
{
    return @"PDFVC";
}

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
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.iUploadAmount = 0;
    self.iUploadTotal = 0;
    
    self.title = [Localizer getLocalizedText:@"Export"];
    
    self.navigationController.toolbarHidden = NO;
    UIBarButtonItem *btnMail = [[UIBarButtonItem alloc] initWithTitle:[@"Email" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(showMailPDF:)];
    UIBarButtonItem *btnPrint = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Print"] style:UIBarButtonItemStyleBordered target:self action:@selector(doPrint:)];
    UIBarButtonItem *btnDropbox = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Save"] style:UIBarButtonItemStyleBordered target:self action:@selector(doDropbox:)];
    UIBarButtonItem *flexSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSArray *tbItems = @[btnPrint, flexSpace,btnMail, flexSpace, btnDropbox];
    [self setToolbarItems: tbItems];
    
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(closeMe:)];
    self.navigationItem.rightBarButtonItem = btnClose;
    
    [Flurry logEvent:@"Export tapped and local PDF created"];
}

-(void)showMailPDF:(id)sender
{

    MFMailComposeViewController *mailComposer = [[MFMailComposeViewController alloc] init];
    
    mailComposer.mailComposeDelegate = self;
    
    [mailComposer setSubject:[@"SmartExpense Report" localize]];
    
    [mailComposer addAttachmentData:[NSData dataWithContentsOfFile:self.pdfFilePath]
                           mimeType:@"application/pdf" fileName:@"report.pdf"];
    
    [self presentViewController:mailComposer animated:YES completion:nil];
}

#pragma mark Mail Stuff
- (void)mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)err
{
	NSString *alertTitle = nil;
	NSString *alertMessage = nil;
	
	switch (result)
	{
		case MFMailComposeResultSent:
            [Flurry logEvent:@"Emailed a Report from PDF"];
			alertTitle = [@"Sent" localize];
			alertMessage = [@"An email has been sent containing your report." localize];
			break;
		case MFMailComposeResultSaved:
			alertTitle = [@"Saved" localize];
			alertMessage = [@"email saved into your drafts" localize];
			break;
		case MFMailComposeResultFailed:
			alertTitle = [@"Failed" localize];
			if (err != nil)
				alertMessage = [err localizedFailureReason];
			else
				alertMessage = @"";
			break;
		default:
			break;
	}
	
	[controller dismissViewControllerAnimated:YES completion:nil];
    
	if (alertTitle != nil && alertMessage != nil)
	{
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle: alertTitle
							  message: alertMessage
							  delegate:nil 
							  cancelButtonTitle:@"Close"
							  otherButtonTitles:nil];
		[alert show];
	}
    
}

#pragma mark - Close Methods
-(void) closeMe:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Print
-(void) doPrint:(id)sender
{
    
    [Flurry logEvent:@"Printed a report from PDF"];
    
    UIPrintInteractionController *pic = [UIPrintInteractionController sharedPrintController];
    pic.delegate = self;
    
    UIPrintInfo *printInfo = [UIPrintInfo printInfo];
    printInfo.outputType = UIPrintInfoOutputGeneral;
    printInfo.jobName = @"Sample";
    pic.printInfo = printInfo;
    
    pic.showsPageRange = YES;
    
    void (^completionHandler)(UIPrintInteractionController *, BOOL, NSError *) =
    ^(UIPrintInteractionController *printController, BOOL completed, NSError *error) {
        
        if (!completed && error) {
            NSLog(@"Printing could not complete because of error: %@", error);
        }
    };
    
    pic.printingItem = [NSData dataWithContentsOfFile:self.pdfFilePath];
    
    if ([UIDevice isPad]) {
        [pic presentFromBarButtonItem:sender animated:YES completionHandler:completionHandler];
        
    } else {
        [pic presentAnimated:YES completionHandler:completionHandler];
    }
}

#pragma mark - Dropbox
-(IBAction)doDropbox:(id)sender
{
    NSLog(@"doDropbox this method is not implemented");
}


-(void) uploadRawReceipts:(ReportData*) rpt

{
    NSArray *aReceipts = [[ReportReceiptInfoManager sharedInstance] getEntryReceiptInfoForRpt:report.rptKey]; 

    int i = 1;
    for(EntityReportReceiptInfo *info in aReceipts)
    {
        self.iUploadAmount++;
        i++;
    }

    
}

@end

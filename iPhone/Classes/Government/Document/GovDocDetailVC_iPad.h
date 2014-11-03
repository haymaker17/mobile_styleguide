//
//  GovDocDetailVC_iPad.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BaseDetailVC_iPad.h"
#import "GovDocumentDetail.h"
#import "Receipt.h"
#import "iPadHomeVC.h"
#import "ReceiptEditorDelegate.h"
#import "GovUnappliedExpensesDelegate.h"
#import "GovDocStampVCDelegate.h"

@interface GovDocDetailVC_iPad : BaseDetailVC_iPad <UITableViewDataSource
    , UITableViewDelegate
    , UIAlertViewDelegate
    , ReceiptEditorDelegate
    , GovUnappliedExpensesDelegate
    , GovDocStampVCDelegate>
{
    GovDocumentDetail       *doc;
    Receipt                 *receipt;

    iPadHomeVC				*iPadHome;

    UITableView				*rightTableView;
    UILabel                 *lblExpensesTitle;
    UILabel                 *lblDocTraveler;
    UILabel                 *lblDocName;
    UILabel                 *lblDocType;
    UILabel                 *lblDocDate;
    
    UILabel                 *lblLabelStatus;
    UILabel                 *lblLabelTANumber;
    UILabel                 *lblLabelTrip;
    UILabel                 *lblLabelEmissions;

    UILabel                 *lblStatus;
    UILabel                 *lblTANumber;
    UILabel                 *lblTrip;
    UILabel                 *lblEmissions;
    UILabel                 *lblTotalAmount;
    

    UIButton				*btnStamp;
    BOOL                    stampSuccess;
    
    NSArray                 *_buttonDescriptors;
}

@property BOOL stampSuccess;
@property (strong, nonatomic) GovDocumentDetail         *doc;
@property (strong, nonatomic) Receipt                   *receipt;

@property (nonatomic, strong) IBOutlet UITableView      *rightTableView;
@property (strong, nonatomic) IBOutlet UILabel          *lblExpensesTitle;
@property (strong, nonatomic) IBOutlet UILabel          *lblDocTraveler;
@property (strong, nonatomic) IBOutlet UILabel          *lblDocName;
@property (strong, nonatomic) IBOutlet UILabel          *lblDocType;
@property (strong, nonatomic) IBOutlet UILabel          *lblDocDate;
@property (strong, nonatomic) IBOutlet UILabel          *lblStatus;
@property (strong, nonatomic) IBOutlet UILabel          *lblTANumber;
@property (strong, nonatomic) IBOutlet UILabel          *lblTrip;
@property (strong, nonatomic) IBOutlet UILabel          *lblEmissions;
@property (strong, nonatomic) IBOutlet UILabel          *lblTotalAmount;

@property (strong, nonatomic) IBOutlet UILabel          *lblLabelStatus;
@property (strong, nonatomic) IBOutlet UILabel          *lblLabelTANumber;
@property (strong, nonatomic) IBOutlet UILabel          *lblLabelTrip;
@property (strong, nonatomic) IBOutlet UILabel          *lblLabelEmissions;

// Left and right panes
@property (strong, nonatomic) IBOutlet UIView           *leftPaneView;
@property (strong, nonatomic) IBOutlet UIView           *upperLeftPaneView;
@property (nonatomic, strong) UIButton                  *btnStamp;

@property (strong, nonatomic) IBOutlet UIButton         *button0;
@property (strong, nonatomic) IBOutlet UIButton         *button1;
@property (strong, nonatomic) IBOutlet UIButton         *button2;
@property (strong, nonatomic) IBOutlet UIButton         *button3;
@property (strong, nonatomic) IBOutlet UIButton         *button4;
@property (strong, nonatomic) IBOutlet UIButton         *button5;
@property (strong, nonatomic) IBOutlet UIButton         *button6;
@property (strong, nonatomic) IBOutlet UIButton         *button7;


@property (strong, nonatomic) IBOutlet UILabel          *labelOnButton0;
@property (strong, nonatomic) IBOutlet UILabel          *labelOnButton1;
@property (strong, nonatomic) IBOutlet UILabel          *labelOnButton2;
@property (strong, nonatomic) IBOutlet UILabel          *labelOnButton3;
@property (strong, nonatomic) IBOutlet UILabel          *labelOnButton4;
@property (strong, nonatomic) IBOutlet UILabel          *labelOnButton5;
@property (strong, nonatomic) IBOutlet UILabel          *labelOnButton6;
@property (strong, nonatomic) IBOutlet UILabel          *labelOnButton7;

@property (strong, nonatomic) iPadHomeVC				*iPadHome;

//-(void) adjustForLandscape;
//-(void) adjustForPortrait;
-(void) setSeedData:(NSMutableDictionary*)pBag;
- (IBAction) buttonPressed:(id)sender;
+(void)showDocDetailWithTraveler:(NSString*)travId withDocName:(NSString*) docName withDocType:(NSString*) docType;

@end

//
//  GovDocDetailVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/7/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "GovDocumentDetail.h"
#import "Receipt.h"
#import "ReceiptEditorDelegate.h"
#import "GovDocTripTypeCode.h"
#import "OptionsSelectDelegate.h"

@interface GovDocDetailVC : MobileViewController<UITableViewDataSource, UITableViewDelegate, ReceiptEditorDelegate, OptionsSelectDelegate>

@property (strong, nonatomic) NSMutableArray            *sections;
@property (strong, nonatomic) IBOutlet UILabel			*lblName;
@property (strong, nonatomic) IBOutlet UILabel			*lblAmount;
@property (strong, nonatomic) IBOutlet UILabel			*lblDocName;
@property (strong, nonatomic) IBOutlet UILabel			*lblDocType;
@property (strong, nonatomic) IBOutlet UILabel			*lblDates;
@property (strong, nonatomic) IBOutlet UILabel			*lblProp1;
@property (strong, nonatomic) IBOutlet UILabel			*lblVal1;
@property (strong, nonatomic) IBOutlet UILabel			*lblProp2;
@property (strong, nonatomic) IBOutlet UILabel			*lblVal2;
@property (strong, nonatomic) IBOutlet UILabel			*lblProp3;
@property (strong, nonatomic) IBOutlet UILabel			*lblVal3;
@property (strong, nonatomic) IBOutlet UILabel			*lblProp4;
@property (strong, nonatomic) IBOutlet UILabel			*lblVal4;
@property (strong, nonatomic) IBOutlet UIImageView		*img1;
@property (strong, nonatomic) IBOutlet UIImageView		*img2;
@property (strong, nonatomic) IBOutlet UITableView      *tableList;

@property (strong, nonatomic) GovDocumentDetail         *doc;
@property (strong, nonatomic) Receipt                   *receipt;
@property (strong, nonatomic) GovDocTripTypeCode        *selectedTripTypeCode;

+(void)showDocDetail:(UIViewController*)pvc withTraveler:(NSString*)travId withDocName:(NSString*) docName withDocType:(NSString*) docType withGtmDocType:(NSString*) gtmDocType;
+(void)showAuthFromRootWithDocName:(NSString*) docName withDocType:(NSString*) docType;
+(void)pushAuthWithDocName:(NSString*) docName withDocType:(NSString*) docType;
@end

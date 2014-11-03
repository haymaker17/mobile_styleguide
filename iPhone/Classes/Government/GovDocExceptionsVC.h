//
//  GovDocExceptionsVC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 12/28/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "GovDocumentDetail.h"
#import "GovDocHeaderShortProtocol.h"

@interface GovDocExceptionsVC : MobileViewController<UITableViewDataSource, UITableViewDelegate, GovDocHeaderShortProtocol, TextEditDelegate>
{
    GovDocumentDetail               *doc;
}

@property (strong, nonatomic) IBOutlet UILabel			*lblName;
@property (strong, nonatomic) IBOutlet UILabel			*lblAmount;
@property (strong, nonatomic) IBOutlet UILabel			*lblStatus;
@property (strong, nonatomic) IBOutlet UILabel			*lblDocName;
@property (strong, nonatomic) IBOutlet UILabel			*lblDates;
@property (strong, nonatomic) IBOutlet UIImageView		*img1;
@property (strong, nonatomic) IBOutlet UIImageView		*img2;
@property (strong, nonatomic) IBOutlet UITableView      *tableList;

@property (strong, nonatomic) GovDocumentDetail         *doc;

+(void)showDocExceptions:(UIViewController*)pvc withDoc:(GovDocumentDetail*) docDetail;
@end

//
//  GovDocPerDiemVC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "GovDocHeaderShortProtocol.h"
#import "GovDocumentDetail.h"

@interface GovDocPerDiemVC : MobileViewController<UITableViewDataSource,UITableViewDelegate, GovDocHeaderShortProtocol>
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

@property (nonatomic, strong) GovDocumentDetail *doc;

+(void)showDocPerDiem:(UIViewController*)pvc withDoc:(GovDocumentDetail*) docDetail;

@end

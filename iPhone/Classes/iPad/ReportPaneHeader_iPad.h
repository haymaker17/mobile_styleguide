//
//  ReportPaneHeader_iPad.h
//  ConcurMobile
//
//  Created by charlottef on 3/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ReportPaneHeader_iPad : UIView

@property (strong, nonatomic) IBOutlet UILabel  *reportName;
@property (strong, nonatomic) IBOutlet UILabel  *reportPurpose;
@property (strong, nonatomic) IBOutlet UILabel  *reportDate;
@property (strong, nonatomic) IBOutlet UILabel  *reportStatus;

@property (weak, nonatomic) IBOutlet UITapGestureRecognizer *rptHeaderTapGesture;

@end

//
//  TripRejectCommentVC.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 03/05/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "TripToApprove.h"
#import "ReportRejectionDelegate.h"

@interface TripRejectCommentVC : MobileViewController

@property (strong, nonatomic) IBOutlet UITextView *txtComment;
@property (strong, nonatomic) id <ReportRejectionDelegate> tripRejectDelegate;

@end

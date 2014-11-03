//
//  SafetyCheckInVC.h
//  ConcurMobile
//
//  Created by yiwen on 8/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FormViewControllerBase.h"
#import "FindMe.h"
#import "FindMeDelegate.h"

@interface SafetyCheckInVC : FormViewControllerBase <UIAlertViewDelegate, FindMeDelegate>
{
    FindMe          *findMe;
    UIButton        *btnCheckIn;
    int             counter;
    BOOL            doReload;
}

@property (strong, nonatomic) FindMe        *findMe;
@property (strong, nonatomic) UIButton      *btnCheckIn;
- (void)setSeedData:(NSDictionary*)pBag;

@end

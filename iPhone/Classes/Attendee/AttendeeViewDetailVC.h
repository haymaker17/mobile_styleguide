//
//  AttendeeViewDetailVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 5/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FormViewControllerBase.h"
#import "AttendeeData.h"

@interface AttendeeViewDetailVC : FormViewControllerBase
{
    AttendeeData						*attendee;
    NSArray                             *atnColumns;
}

@property (nonatomic, strong) AttendeeData						*attendee;
@property (nonatomic, strong) NSArray							*atnColumns;

@end

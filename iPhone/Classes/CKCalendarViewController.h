//
//  CKCalendarViewController.h
//  Jarvis
//
//  Created by Wanny Morellato on 9/4/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CKCalendarView.h"

@interface CKCalendarViewController : UIViewController<CKCalendarDelegate>

- (id)init;

@property (strong,nonatomic) CKCalendarView *calendarView;

@property (weak, nonatomic) NSDate *initialDate;

@property (copy,nonatomic) void(^onDateSelected)(NSDate *date);



@end

//
//  SlidingMessageViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/23/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface SlidingMessageViewController : UIViewController 
{
	UILabel   *titleLabel;              
	UILabel   *msgLabel; 
	NSString	*isRefresh;
}

@property (nonatomic, retain) NSString *isRefresh;

- (id)initWithTitle:(NSString *)title message:(NSString *)msg;
- (void)showMsgWithDelay:(int)delay;


@end

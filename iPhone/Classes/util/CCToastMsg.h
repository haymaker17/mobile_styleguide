//
//  CCToastMsg.h
//  ConcurMobile
//
//  Created by laurent mery on 12/11/2014.
//  Copyright (c) 2014 concur. All rights reserved.
//
/*
 
 Toast Message is a class to display indormation, warning
 with design information provide by the style guide
 
 init this class on the viewdidload of your controller
 to be able to manage multiple toast message appeared
 
 */

#import <UIKit/UIKit.h>

@interface CCToastMsg : NSObject

// we centered toast message relatively to this view reference
@property (nonatomic, retain) UIView *view;

-(id)initWithView:(UIView*)view;
- (void)toastWarningMessage:(NSString *)message;

@end

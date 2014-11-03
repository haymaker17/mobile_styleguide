//
//  AbstractIpmViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 1/5/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AbstractIpmViewController : UIViewController

@property (strong, nonatomic) MessageCenterMessage *message;

- (id)initWithMessageId:(NSString *)messageId;

@end

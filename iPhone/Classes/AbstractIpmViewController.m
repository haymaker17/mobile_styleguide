//
//  AbstractIpmViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 1/5/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AbstractIpmViewController.h"

@interface AbstractIpmViewController ()

@end

@implementation AbstractIpmViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        // Custom initialization
    }
    
    return self;
}

- (id)initWithMessageId:(NSString *)messageId {
    self = [super init];
    
    if (self) {
        MessageCenterMessage *message = [[MessageCenterManager sharedInstance] messageForId:messageId];
        
        self.message = message;
    }
    
    return self;
}

@end
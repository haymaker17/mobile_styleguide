//
//  TextViewController.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 19/02/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface TextViewController : MobileViewController

- (instancetype)initWithTitle:(NSString *)title;
@property (nonatomic, strong) NSString *text;

@end

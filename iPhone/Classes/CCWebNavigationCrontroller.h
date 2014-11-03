//
//  CCWebNavigationCrontroller.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 1/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CCWebBrowser.h"

@interface CCWebNavigationCrontroller : UINavigationController

- (instancetype)initWithTitle:(NSString*)title leftBarButtonItem:(UIBarButtonItem*)leftBarButtonItem;

@property (nonatomic,strong) CCWebBrowser *browser;

@end

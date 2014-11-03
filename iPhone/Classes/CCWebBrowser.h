//
//  CCWebBrowser.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 1/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CCWebBrowser : UIViewController <UIWebViewDelegate>

- (instancetype)initWithTitle:(NSString*)title;

@property (nonatomic,copy) NSString *URLString;

@end

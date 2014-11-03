//
//  TabBarViewController.h
//  ConcurMobile
//
//  Created by Shifan Wu on 11/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TabBarViewController : UICollectionViewController

@property (copy,nonatomic) void (^selectOption)( NSDictionary *option);

@end

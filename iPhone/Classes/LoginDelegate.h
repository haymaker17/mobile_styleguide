//
//  LoginDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol LoginDelegate <NSObject>
- (void)dismissYourself:(UIViewController*)vc;
@end

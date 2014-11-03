//
//  IgniteChatterPostDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol IgniteChatterPostDelegate <NSObject>
-(void) didPostToChatter;
-(void) closeChatterPostVC;
@end

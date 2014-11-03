//
//  IgniteItinShareTripDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol IgniteItinShareTripDelegate <NSObject>
-(void) didShareTrip;
-(void) closeShareTripVC;
@end

//
//  TextEditDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 5/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@protocol TextEditDelegate

-(void) textUpdated:(NSObject*) context withValue:(NSString*) value;

@end

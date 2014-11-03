//
//  WeakReference.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 11/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface WeakReference : NSObject
{
	NSObject*  __unsafe_unretained ref;
}
// weak scope causes error:bjc[2478]: cannot form weak reference to instance (0x10360f000) of class ExpenseTypesViewController
@property (nonatomic, unsafe_unretained) NSObject* ref;

+(WeakReference*) toObject:(NSObject*)object;

@end

//
//  NSArray+Additions.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 8/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NSArray+Additions.h"

@implementation NSArray (Additions)


+ (id)arrayWithNotNilObjects:(NSInteger)count, ...  {
    
    va_list argumentList;
    NSMutableArray *tmp = [NSMutableArray arrayWithCapacity:count];
    
    id eachObject;
    va_start(argumentList, count); // Start scanning for arguments after firstObject.
    
    for (NSInteger i = 0 ; i<count ; i++) {
        eachObject = va_arg(argumentList, id);
        if (eachObject != nil) {
            [tmp addObject: eachObject]; // that isn't nil, add it to self's contents.
        }
    }
    va_end(argumentList);
    
    return tmp;
    
}

@end

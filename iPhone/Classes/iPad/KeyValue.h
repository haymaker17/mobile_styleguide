//
//  KeyValue.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface KeyValue : NSObject {
	NSString		*key, *val, *selectorName, *valType, *label;
}

@property (strong, nonatomic) NSString		*key;
@property (strong, nonatomic) NSString		*val;
@property (strong, nonatomic) NSString		*selectorName;
@property (strong, nonatomic) NSString		*valType;
@property (strong, nonatomic) NSString		*label;

@end

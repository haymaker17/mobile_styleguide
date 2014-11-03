//
//  EntityTravelCustomFieldsMock.m
//  ConcurMobile
//
//  Created by Richard Puckett on 10/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EntityTravelCustomFieldsMock.h"

@implementation EntityTravelCustomFieldsMock

- (NSString *)description {
    return [NSString stringWithFormat:@"{ \"%@\", %@, %@, %@ }",
            self.dataType,
            self.minLength,
            self.maxLength,
            self.required];
}

@end

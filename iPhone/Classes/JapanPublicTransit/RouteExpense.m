//
//  RouteExpense.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "RouteExpense.h"

@implementation RouteExpense

- (id)init {
    self = [super init];
    
    if (self) {
        self.route = [[Route alloc] init];
        
        CFUUIDRef uuidRef = CFUUIDCreate(kCFAllocatorDefault);
        self.uuid = (NSString *) CFBridgingRelease(CFUUIDCreateString(NULL, uuidRef));
        CFRelease(uuidRef);
        
        self.purpose = [[NSString alloc] init];
        self.comment = [[NSString alloc] init];
    }
    
    return self;
}

- (NSString *)description {
    return [NSString stringWithFormat: @"RouteExpense: { uuid: %@, comment: %@, purpose: %@, isPersonal: %@ route: %@ }",
            self.uuid, self.comment, self.purpose, self.isPersonalExpense ? @"YES" : @"NO", self.route];
}

#pragma mark - NSCoding

-(void)encodeWithCoder:(NSCoder *)encoder {
    [encoder encodeObject:self.route forKey:@"route"];
    [encoder encodeObject:self.purpose forKey:@"purpose"];
    [encoder encodeObject:self.comment forKey:@"comment"];
    [encoder encodeBool:self.isFavorite forKey:@"is_favorite"];
    [encoder encodeBool:self.isPersonalExpense forKey:@"is_personal_expense"];
    [encoder encodeObject:self.uuid forKey:@"uuid"];
}

-(id)initWithCoder:(NSCoder *)decoder {
    self.route = [decoder decodeObjectForKey:@"route"];
    self.purpose = [decoder decodeObjectForKey:@"purpose"];
    self.comment = [decoder decodeObjectForKey:@"comment"];
    self.isFavorite = [decoder decodeBoolForKey:@"is_favorite"];
    self.isPersonalExpense = [decoder decodeBoolForKey:@"is_personal_expense"];
    self.uuid = [decoder decodeObjectForKey:@"uuid"];
    
    return self;
}

#pragma mark - NSCopying

-(id)copyWithZone:(NSZone *)zone {
    RouteExpense *clone = [[RouteExpense alloc] init];
    
    clone.uuid = [self.uuid copy];
    clone.route = [self.route copy];
    clone.purpose = [self.purpose copy];
    clone.comment = [self.comment copy];
    clone.isFavorite = self.isFavorite;
    clone.isPersonalExpense = self.isPersonalExpense;
    
    return clone;
}

@end

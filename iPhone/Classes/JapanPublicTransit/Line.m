//
//  Line.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/11/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Line.h"

@implementation Line

- (NSString *)description {
    return [NSString stringWithFormat:@"Line: { key: %@, name: %@ }",
            self.key, self.name];
}

+ (Line *)empty {
    Line *line = [[Line alloc] init];
    
    return line;
}

#pragma mark - NSCoding

-(void)encodeWithCoder:(NSCoder *)encoder {
    [encoder encodeObject:self.key forKey:@"key"];
    [encoder encodeObject:self.name forKey:@"name"];
}

-(id)initWithCoder:(NSCoder *)decoder {
    self.key = [decoder decodeObjectForKey:@"key"];
    self.name = [decoder decodeObjectForKey:@"name"];
    
    return self;
}

@end

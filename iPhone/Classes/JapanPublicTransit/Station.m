//
//  Station.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Localizer.h"
#import "Station.h"

@implementation Station

+ (Station *)empty {
    Station *station = [[Station alloc] init];
    
    return station;
}

+ (Station *)none {
    Station *station = [[Station alloc] init];
    
    station.name = [Localizer getLocalizedText:@"None"];
    
    return station;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"Station: { key: %@, name: %@ }",
            self.key, self.name];
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.key = 0;
    }
    
    return self;
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

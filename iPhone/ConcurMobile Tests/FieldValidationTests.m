//
//  FieldValidationTests.m
//  ConcurMobile
//
//  Created by Richard Puckett on 10/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>

#import "EntityTravelCustomFieldsInterface.h"
#import "EntityTravelCustomFieldsMock.h"
#import "NSStringAdditions.h"
#import "NSString+Additions.h"
#import "ReportEntryViewController.h"

@interface FieldValidationTests : XCTestCase

@end

@implementation FieldValidationTests

- (void)setUp {
    [super setUp];
    NSLog(@"XXXXXXXXXXXXX instance setUp");
}

- (void)tearDown {
    NSLog(@"instance tearDown");
    [super tearDown];
}

- (void)testValidation {
    typedef struct refactor_data_t {
        char *dataType;
        int minLength;
        int maxLength;
        int required;
    } refactor_data_t;
    
    refactor_data_t t[] = {
        { "number", 0, 0, 1 },
        { "number", 0, 1, 1 },
        { "number", 1, 0, 0 },
        { "number", 0, -1, 1 }
    };
    
    for (int i = 0; i < sizeof(t)/sizeof(refactor_data_t); i++) {
        refactor_data_t *p = &t[i];
        
        EntityTravelCustomFieldsMock *tcf = [[EntityTravelCustomFieldsMock alloc] init];
        tcf.dataType = [NSString stringWithUTF8String:p->dataType];
        tcf.minLength = [NSNumber numberWithInt:p->minLength];
        tcf.maxLength = [NSNumber numberWithInt:p->maxLength];
        tcf.required = [NSNumber numberWithInt:p->required];
        
        BOOL isLegacyValid;
        BOOL isNewValid;
        
        isLegacyValid = [self legacyValidatorForString:@" " andField:tcf];
        isNewValid = [self newValidatorForString:@" " andField:tcf];
        
        if (isLegacyValid != isNewValid) {
            XCTFail(@"Refactor failed for %@.", tcf);
            break;
        }
    }
}

- (void) testConditionalFields
{
    ReportEntryViewController *vcBase = [[ReportEntryViewController alloc] initWithCloseButton:NO];
 
    ConditionalFieldsList *cfl = [[ConditionalFieldsList alloc] init];
    
    ConditionalFieldAction *action = [[ConditionalFieldAction alloc] init];
    action.action = @"SHOW";
    action.field = 232;
    action.Access = @"RW";
    
    [[cfl conditionalFieldListData] add:action];
    
    FormFieldData *formFieldData1 = [[FormFieldData alloc] init];
    formFieldData1.formFieldKey = @"231";

    FormFieldData *formFieldData2 = [[FormFieldData alloc] init];
    formFieldData2.formFieldKey = @"232";
    
    NSMutableArray *dataFields = [[NSMutableArray alloc] init];
    
    [dataFields addObject:formFieldData1];
    [dataFields addObject:formFieldData2];
    
    BOOL fRefresh = [ vcBase updateDynamicFields:cfl
                                          fields:dataFields];
    if (!fRefresh)
    {
        XCTFail("Refreshing on a structure should return true");
    }
    
}

- (BOOL)validateRequiredFieldForString:(NSString *)text andField:(id <EntityTravelCustomFieldsInterface>)tcf {
    int length = (int)text.length;
    
    if ([tcf.required boolValue] == NO) {
        return YES;
    }
    
    if ([tcf.required boolValue] == YES && length > 0 && [text lengthIgnoreWhitespace]) {
        return YES;
    }
    
    return NO;
}

- (BOOL)validateSomethingElseForString:(NSString *)text andField:(id <EntityTravelCustomFieldsInterface>)tcf {
    int min = [tcf.minLength intValue];
    int max = [tcf.maxLength intValue];
    int length = (int)text.length;
    
    return (min <= max && length >= min && length <= max) || min > max  || ([tcf.dataType isEqualToString:@"number"] || (min < 0 && max < 0));
}

// From CustomFieldTextEditor:closeView
//
- (BOOL)legacyValidatorForString:(NSString *)text andField:(id <EntityTravelCustomFieldsInterface>)tcf {
    int min = [tcf.minLength intValue];
    int max = [tcf.maxLength intValue];
    int length = (int)text.length;
    
    if ((([tcf.required boolValue] == YES && length > 0 && [text lengthIgnoreWhitespace]) || [tcf.required boolValue] == NO ) && ((min <= max && length >= min && length <= max) || min > max  || ([tcf.dataType isEqualToString:@"number"] || (min < 0 && max < 0)))) {
        return YES;
    }

    return NO;
}

- (BOOL)newValidatorForString:(NSString *)text andField:(id <EntityTravelCustomFieldsInterface>)tcf {
    return [self validateRequiredFieldForString:text andField:tcf] && [self validateSomethingElseForString:text andField:tcf];
}

@end
